-- =========================================================
-- LOGISTICA DELTA - BASE DE DATOS (VERSION SIMPLIFICADA)
-- Compatible con MySQL/MariaDB (XAMPP / phpMyAdmin)
-- Motor: InnoDB | Charset: utf8mb4
-- 17 tablas (version anterior tenia 28)
-- =========================================================

CREATE DATABASE IF NOT EXISTS logistica_delta
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE logistica_delta;

SET FOREIGN_KEY_CHECKS = 0;

-- =========================================================
-- 1. USUARIOS
-- (antes "roles" era una tabla aparte; ahora es un ENUM
--  porque los 5 roles son un catalogo cerrado y fijo)
-- =========================================================
CREATE TABLE usuarios (
    id_usuario      INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(80)  NOT NULL,
    apellido        VARCHAR(80)  NOT NULL,
    dni             VARCHAR(15)  NULL,
    email           VARCHAR(120) NOT NULL,
    nombre_usuario  VARCHAR(60)  NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    rol             ENUM('ADMINISTRADOR','OPERADOR_DEPOSITO','CHOFER','ADMINISTRACION','CLIENTE') NOT NULL,
    activo          TINYINT(1) NOT NULL DEFAULT 1,
    fecha_creacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_login    DATETIME NULL,
    UNIQUE KEY uq_usuarios_email (email),
    UNIQUE KEY uq_usuarios_nombre_usuario (nombre_usuario),
    UNIQUE KEY uq_usuarios_dni (dni)
) ENGINE=InnoDB;

-- =========================================================
-- 2. ZONAS
-- =========================================================
CREATE TABLE zonas (
    id_zona      INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(80) NOT NULL,
    cobertura    ENUM('CABA_GBA','INTERIOR') NOT NULL,
    descripcion  VARCHAR(255) NULL,
    activo       TINYINT(1) NOT NULL DEFAULT 1,
    UNIQUE KEY uq_zonas_nombre (nombre)
) ENGINE=InnoDB;

-- =========================================================
-- 3. CLIENTES
-- (el descuento corporativo ahora vive aca en vez de una
--  tabla "tarifas_cliente" aparte)
-- =========================================================
CREATE TABLE clientes (
    id_cliente            INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario            INT NOT NULL,
    tipo_cliente          ENUM('PARTICULAR','CORPORATIVO') NOT NULL DEFAULT 'PARTICULAR',
    razon_social          VARCHAR(150) NULL,
    cuit_dni              VARCHAR(20)  NULL,
    telefono              VARCHAR(30)  NULL,
    direccion             VARCHAR(200) NULL,
    estado_validacion     ENUM('PENDIENTE','VALIDADO','RECHAZADO') NOT NULL DEFAULT 'PENDIENTE',
    aprobado_corporativo  TINYINT(1) NOT NULL DEFAULT 0,
    descuento_porcentaje  DECIMAL(5,2) NOT NULL DEFAULT 0,
    fecha_registro        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_clientes_usuario (id_usuario),
    CONSTRAINT fk_clientes_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- =========================================================
-- 4. TARIFAS
-- =========================================================
CREATE TABLE tarifas (
    id_tarifa       INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    id_zona         INT NOT NULL,
    tipo_envio      VARCHAR(60)  NOT NULL,
    precio_base     DECIMAL(10,2) NOT NULL,
    precio_por_kg   DECIMAL(10,2) NOT NULL DEFAULT 0,
    precio_por_m3   DECIMAL(10,2) NOT NULL DEFAULT 0,
    recargo_fragil  DECIMAL(10,2) NOT NULL DEFAULT 0,
    vigente_desde   DATE NOT NULL,
    vigente_hasta   DATE NULL,
    activo          TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_tarifas_zona FOREIGN KEY (id_zona) REFERENCES zonas(id_zona)
) ENGINE=InnoDB;

-- =========================================================
-- 5. DEPOSITOS
-- =========================================================
CREATE TABLE depositos (
    id_deposito  INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(80) NOT NULL,
    direccion    VARCHAR(200) NOT NULL,
    activo       TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- =========================================================
-- 6. UBICACIONES
-- (antes eran 3 tablas: sectores, estantes, posiciones.
--  Ahora es una sola tabla con esas 3 columnas, pero sigue
--  siendo un catalogo controlado: no se puede escribir
--  libremente un nombre de ubicacion)
-- =========================================================
CREATE TABLE ubicaciones (
    id_ubicacion  INT AUTO_INCREMENT PRIMARY KEY,
    id_deposito   INT NOT NULL,
    sector        VARCHAR(20) NOT NULL,
    estante       VARCHAR(20) NOT NULL,
    posicion      VARCHAR(20) NOT NULL,
    ocupada       TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uq_ubicacion (id_deposito, sector, estante, posicion),
    CONSTRAINT fk_ubicaciones_deposito FOREIGN KEY (id_deposito) REFERENCES depositos(id_deposito)
) ENGINE=InnoDB;

-- =========================================================
-- 7. VEHICULOS
-- (la restriccion por zona ahora es un campo simple aca
--  en vez de una tabla aparte; cubre el caso comun de
--  "este vehiculo no puede entrar a tal zona")
-- =========================================================
CREATE TABLE vehiculos (
    id_vehiculo            INT AUTO_INCREMENT PRIMARY KEY,
    patente                VARCHAR(15) NOT NULL,
    tipo                   VARCHAR(40) NOT NULL,
    marca                  VARCHAR(50) NULL,
    modelo                 VARCHAR(50) NULL,
    anio                   SMALLINT NULL,
    capacidad_peso_kg      DECIMAL(10,2) NOT NULL,
    capacidad_volumen_m3   DECIMAL(10,2) NOT NULL,
    estado                 ENUM('DISPONIBLE','EN_RUTA','MANTENIMIENTO','INACTIVO') NOT NULL DEFAULT 'DISPONIBLE',
    id_zona_restringida    INT NULL,
    motivo_restriccion     VARCHAR(200) NULL,
    UNIQUE KEY uq_vehiculos_patente (patente),
    CONSTRAINT fk_vehiculos_zona FOREIGN KEY (id_zona_restringida) REFERENCES zonas(id_zona)
) ENGINE=InnoDB;

-- =========================================================
-- 8. CHOFERES
-- =========================================================
CREATE TABLE choferes (
    id_chofer                  INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario                 INT NOT NULL,
    licencia                   VARCHAR(30) NOT NULL,
    categoria_licencia         VARCHAR(10) NOT NULL,
    fecha_vencimiento_licencia DATE NOT NULL,
    estado                     ENUM('ACTIVO','INACTIVO','SUSPENDIDO') NOT NULL DEFAULT 'ACTIVO',
    UNIQUE KEY uq_choferes_usuario (id_usuario),
    UNIQUE KEY uq_choferes_licencia (licencia),
    CONSTRAINT fk_choferes_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- =========================================================
-- 9. CONTACTOS
-- (antes "remitentes" y "destinatarios" eran dos tablas
--  identicas en estructura; ahora es una sola con un
--  campo "tipo" que distingue cual es cual)
-- =========================================================
CREATE TABLE contactos (
    id_contacto  INT AUTO_INCREMENT PRIMARY KEY,
    tipo         ENUM('REMITENTE','DESTINATARIO') NOT NULL,
    nombre       VARCHAR(120) NOT NULL,
    telefono     VARCHAR(30) NULL,
    email        VARCHAR(120) NULL,
    direccion    VARCHAR(200) NOT NULL,
    id_zona      INT NULL,
    id_cliente   INT NULL,
    CONSTRAINT fk_contactos_zona    FOREIGN KEY (id_zona)    REFERENCES zonas(id_zona),
    CONSTRAINT fk_contactos_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente)
) ENGINE=InnoDB;

-- =========================================================
-- 10. PAQUETES
-- =========================================================
CREATE TABLE paquetes (
    id_paquete        INT AUTO_INCREMENT PRIMARY KEY,
    codigo            VARCHAR(20) NOT NULL,
    descripcion       VARCHAR(255) NULL,
    peso_kg           DECIMAL(10,3) NOT NULL,
    largo_cm          DECIMAL(10,2) NULL,
    ancho_cm          DECIMAL(10,2) NULL,
    alto_cm           DECIMAL(10,2) NULL,
    cantidad_bultos   INT NOT NULL DEFAULT 1,
    tipo_mercaderia   VARCHAR(80) NOT NULL,
    valor_declarado   DECIMAL(12,2) NULL,
    es_fragil         TINYINT(1) NOT NULL DEFAULT 0,
    fecha_registro    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_paquetes_codigo (codigo)
) ENGINE=InnoDB;

-- =========================================================
-- 11. RUTAS
-- =========================================================
CREATE TABLE rutas (
    id_ruta        INT AUTO_INCREMENT PRIMARY KEY,
    fecha          DATE NOT NULL,
    origen         VARCHAR(200) NOT NULL,
    destino        VARCHAR(200) NOT NULL,
    recorrido      TEXT NULL,
    observaciones  VARCHAR(255) NULL,
    id_vehiculo    INT NOT NULL,
    id_chofer      INT NOT NULL,
    id_zona        INT NULL,
    estado         ENUM('PLANIFICADA','EN_CURSO','FINALIZADA','CANCELADA') NOT NULL DEFAULT 'PLANIFICADA',
    CONSTRAINT fk_rutas_vehiculo FOREIGN KEY (id_vehiculo) REFERENCES vehiculos(id_vehiculo),
    CONSTRAINT fk_rutas_chofer   FOREIGN KEY (id_chofer)   REFERENCES choferes(id_chofer),
    CONSTRAINT fk_rutas_zona     FOREIGN KEY (id_zona)     REFERENCES zonas(id_zona)
) ENGINE=InnoDB;

-- =========================================================
-- 12. ENVIOS
-- (el estado ahora es ENUM en vez de tabla "estados_envio":
--  un ENUM ya impide escribir un estado invalido, que era
--  justo el objetivo. Ademas "id_ruta" y "orden_parada"
--  reemplazan a la tabla "asignaciones_transporte", porque
--  un envio viaja en una sola ruta a la vez)
-- =========================================================
CREATE TABLE envios (
    id_envio                INT AUTO_INCREMENT PRIMARY KEY,
    codigo_envio            VARCHAR(20) NOT NULL,
    id_cliente              INT NOT NULL,
    id_remitente            INT NOT NULL,
    id_destinatario         INT NOT NULL,
    id_paquete              INT NOT NULL,
    estado                  ENUM('REGISTRADO','RECIBIDO_DEPOSITO','PREPARADO','EN_TRANSITO',
                                  'EN_REPARTO','ENTREGADO','NO_ENTREGADO','DEVUELTO',
                                  'EXTRAVIADO','CANCELADO') NOT NULL DEFAULT 'REGISTRADO',
    id_zona_destino         INT NOT NULL,
    id_tarifa_aplicada      INT NULL,
    precio_final            DECIMAL(12,2) NULL,
    id_ruta                 INT NULL,
    orden_parada             INT NULL,
    fecha_solicitud          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_entrega_estimada  DATE NULL,
    id_usuario_registro     INT NOT NULL,
    activo                  TINYINT(1) NOT NULL DEFAULT 1,
    UNIQUE KEY uq_envios_codigo (codigo_envio),
    UNIQUE KEY uq_envios_paquete (id_paquete),
    CONSTRAINT fk_envios_cliente      FOREIGN KEY (id_cliente)          REFERENCES clientes(id_cliente),
    CONSTRAINT fk_envios_remitente    FOREIGN KEY (id_remitente)        REFERENCES contactos(id_contacto),
    CONSTRAINT fk_envios_destinatario FOREIGN KEY (id_destinatario)     REFERENCES contactos(id_contacto),
    CONSTRAINT fk_envios_paquete      FOREIGN KEY (id_paquete)          REFERENCES paquetes(id_paquete),
    CONSTRAINT fk_envios_zona         FOREIGN KEY (id_zona_destino)     REFERENCES zonas(id_zona),
    CONSTRAINT fk_envios_tarifa       FOREIGN KEY (id_tarifa_aplicada)  REFERENCES tarifas(id_tarifa),
    CONSTRAINT fk_envios_ruta         FOREIGN KEY (id_ruta)             REFERENCES rutas(id_ruta),
    CONSTRAINT fk_envios_usuario_reg  FOREIGN KEY (id_usuario_registro) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- =========================================================
-- 13. MOVIMIENTOS_DEPOSITO
-- (une lo que antes eran dos tablas: "ubicaciones_paquete"
--  y "movimientos_deposito". La ubicacion actual de un
--  paquete es simplemente su ultimo movimiento, por eso
--  se agrega el flag "es_actual")
-- =========================================================
CREATE TABLE movimientos_deposito (
    id_movimiento          INT AUTO_INCREMENT PRIMARY KEY,
    id_paquete             INT NOT NULL,
    id_ubicacion_anterior  INT NULL,
    id_ubicacion_nueva     INT NOT NULL,
    es_actual              TINYINT(1) NOT NULL DEFAULT 1,
    id_usuario             INT NOT NULL,
    fecha_hora             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo                 VARCHAR(200) NOT NULL,
    CONSTRAINT fk_movdep_paquete    FOREIGN KEY (id_paquete)            REFERENCES paquetes(id_paquete),
    CONSTRAINT fk_movdep_ubic_ant   FOREIGN KEY (id_ubicacion_anterior) REFERENCES ubicaciones(id_ubicacion),
    CONSTRAINT fk_movdep_ubic_nueva FOREIGN KEY (id_ubicacion_nueva)    REFERENCES ubicaciones(id_ubicacion),
    CONSTRAINT fk_movdep_usuario    FOREIGN KEY (id_usuario)            REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- =========================================================
-- 14. INTENTOS_ENTREGA
-- (une lo que antes eran "intentos_entrega" y "entregas".
--  Una entrega exitosa es, sencillamente, un intento con
--  resultado = EXITOSO; por eso los datos del receptor
--  quedan aca, como columnas opcionales)
-- =========================================================
CREATE TABLE intentos_entrega (
    id_intento          INT AUTO_INCREMENT PRIMARY KEY,
    id_envio            INT NOT NULL,
    numero_intento      TINYINT NOT NULL,
    fecha               DATE NOT NULL,
    hora                TIME NOT NULL,
    id_chofer           INT NOT NULL,
    resultado           ENUM('EXITOSO','FALLIDO') NOT NULL,
    motivo_falla        ENUM('DESTINATARIO_AUSENTE','DIRECCION_INCORRECTA','DOMICILIO_INACCESIBLE',
                              'RECHAZO_DESTINATARIO','PROBLEMA_PAQUETE','OTRO') NULL,
    receptor_nombre     VARCHAR(120) NULL,
    receptor_documento  VARCHAR(20) NULL,
    observaciones       VARCHAR(255) NULL,
    UNIQUE KEY uq_intento_envio_numero (id_envio, numero_intento),
    CONSTRAINT fk_intentos_envio  FOREIGN KEY (id_envio)  REFERENCES envios(id_envio),
    CONSTRAINT fk_intentos_chofer FOREIGN KEY (id_chofer) REFERENCES choferes(id_chofer)
) ENGINE=InnoDB;

-- =========================================================
-- 15. INCIDENCIAS
-- =========================================================
CREATE TABLE incidencias (
    id_incidencia        INT AUTO_INCREMENT PRIMARY KEY,
    id_envio             INT NOT NULL,
    fecha                DATE NOT NULL,
    hora                 TIME NOT NULL,
    lugar                VARCHAR(150) NOT NULL,
    id_usuario_registro  INT NOT NULL,
    tipo                 ENUM('EXTRAVIO','DANIO','DIRECCION_INCORRECTA','PROBLEMA_ENTREGA','PROBLEMA_PAQUETE','OTRO') NOT NULL,
    descripcion          TEXT NOT NULL,
    estado               ENUM('ABIERTA','EN_INVESTIGACION','RESUELTA','CERRADA') NOT NULL DEFAULT 'ABIERTA',
    resolucion           TEXT NULL,
    fecha_resolucion     DATETIME NULL,
    CONSTRAINT fk_incidencias_envio   FOREIGN KEY (id_envio)            REFERENCES envios(id_envio),
    CONSTRAINT fk_incidencias_usuario FOREIGN KEY (id_usuario_registro) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- =========================================================
-- 16. HISTORIAL
-- (une "historial_estados" e "historial_modificaciones"
--  en una sola tabla de auditoria general, distinguidas
--  por el campo "tipo_evento")
-- =========================================================
CREATE TABLE historial (
    id_historial      INT AUTO_INCREMENT PRIMARY KEY,
    tipo_evento       ENUM('CAMBIO_ESTADO','MODIFICACION') NOT NULL,
    tabla_afectada    VARCHAR(60) NOT NULL,
    registro_id       INT NOT NULL,
    campo_modificado  VARCHAR(60) NULL,
    valor_anterior    VARCHAR(255) NULL,
    valor_nuevo       VARCHAR(255) NULL,
    id_usuario        INT NOT NULL,
    fecha_hora        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    descripcion       VARCHAR(255) NULL,
    CONSTRAINT fk_historial_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- =========================================================
-- 17. COMPROBANTES
-- =========================================================
CREATE TABLE comprobantes (
    id_comprobante      INT AUTO_INCREMENT PRIMARY KEY,
    id_envio            INT NOT NULL,
    numero_comprobante  VARCHAR(30) NOT NULL,
    fecha_emision       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo                VARCHAR(40) NOT NULL,
    ruta_archivo        VARCHAR(255) NULL,
    id_usuario_emisor   INT NOT NULL,
    UNIQUE KEY uq_comprobantes_numero (numero_comprobante),
    CONSTRAINT fk_comprobantes_envio   FOREIGN KEY (id_envio)          REFERENCES envios(id_envio),
    CONSTRAINT fk_comprobantes_usuario FOREIGN KEY (id_usuario_emisor) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- =========================================================
-- INDICES DE CONSULTA FRECUENTE
-- =========================================================
CREATE INDEX idx_envios_estado      ON envios(estado);
CREATE INDEX idx_envios_cliente     ON envios(id_cliente);
CREATE INDEX idx_paquetes_codigo    ON paquetes(codigo);
CREATE INDEX idx_movdep_paquete     ON movimientos_deposito(id_paquete);
CREATE INDEX idx_historial_registro ON historial(tabla_afectada, registro_id);
CREATE INDEX idx_intentos_envio     ON intentos_entrega(id_envio);
CREATE INDEX idx_incidencias_envio  ON incidencias(id_envio);

SET FOREIGN_KEY_CHECKS = 1;
