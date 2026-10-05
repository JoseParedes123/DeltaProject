-- USUARIOS DE DEMOSTRACION PARA LA INTERFAZ CON ROLES
-- Ejecutar DESPUES de logistica_delta_bd_simplificada.sql y datos_demo.sql
USE logistica_delta;

INSERT INTO usuarios(nombre,apellido,dni,email,nombre_usuario,password_hash,rol,activo)
VALUES ('Ana','García','30111223','ana.admin@delta.local','admin',SHA2('admin123',256),'ADMINISTRADOR',1)
ON DUPLICATE KEY UPDATE rol='ADMINISTRADOR', activo=1;

INSERT INTO usuarios(nombre,apellido,dni,email,nombre_usuario,password_hash,rol,activo)
VALUES ('Laura','Sosa','30111224','laura.admin@delta.local','administracion',SHA2('admin123',256),'ADMINISTRACION',1)
ON DUPLICATE KEY UPDATE rol='ADMINISTRACION', activo=1;

INSERT INTO usuarios(nombre,apellido,dni,email,nombre_usuario,password_hash,rol,activo)
VALUES ('Carlos','Rodríguez','30111225','carlos.chofer@delta.local','chofer',SHA2('chofer123',256),'CHOFER',1)
ON DUPLICATE KEY UPDATE rol='CHOFER', activo=1;

INSERT INTO usuarios(nombre,apellido,dni,email,nombre_usuario,password_hash,rol,activo)
VALUES ('Sofía','Martínez','30111226','sofia.cliente@delta.local','cliente',SHA2('cliente123',256),'CLIENTE',1)
ON DUPLICATE KEY UPDATE rol='CLIENTE', activo=1;

-- Perfil de chofer
INSERT INTO choferes(id_usuario,licencia,categoria_licencia,fecha_vencimiento_licencia,estado)
SELECT id_usuario,'LIC-DELTA-001','B1',DATE_ADD(CURDATE(),INTERVAL 2 YEAR),'ACTIVO'
FROM usuarios u
WHERE u.nombre_usuario='chofer'
AND NOT EXISTS (SELECT 1 FROM choferes c WHERE c.id_usuario=u.id_usuario);

-- Vehículo de demostración
INSERT INTO vehiculos(patente,tipo,marca,modelo,anio,capacidad_peso_kg,capacidad_volumen_m3,estado)
SELECT 'AB123CD','Furgón','Mercedes-Benz','Sprinter',2024,1200,10,'DISPONIBLE'
WHERE NOT EXISTS (SELECT 1 FROM vehiculos WHERE patente='AB123CD');

-- Ruta de demostración para el chofer
SET @chofer=(SELECT c.id_chofer FROM choferes c JOIN usuarios u ON u.id_usuario=c.id_usuario WHERE u.nombre_usuario='chofer' LIMIT 1);
SET @vehiculo=(SELECT id_vehiculo FROM vehiculos WHERE patente='AB123CD' LIMIT 1);
SET @zona=(SELECT id_zona FROM zonas WHERE nombre='CABA' LIMIT 1);

INSERT INTO rutas(fecha,origen,destino,recorrido,observaciones,id_vehiculo,id_chofer,id_zona,estado)
SELECT CURDATE(),'Depósito Central','CABA','Ruta demostrativa','Ruta creada para la presentación',@vehiculo,@chofer,@zona,'EN_CURSO'
WHERE @chofer IS NOT NULL AND @vehiculo IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM rutas r WHERE r.id_chofer=@chofer AND r.fecha=CURDATE() AND r.observaciones='Ruta creada para la presentación');

-- Se asigna un envío existente al chofer para poder probar su pantalla.
UPDATE envios e
JOIN paquetes p ON p.id_paquete=e.id_paquete
SET e.id_ruta=(SELECT r.id_ruta FROM rutas r WHERE r.id_chofer=@chofer AND r.fecha=CURDATE() ORDER BY r.id_ruta DESC LIMIT 1),
    e.estado='EN_TRANSITO'
WHERE p.codigo='DEL-0003' AND @chofer IS NOT NULL;

-- Perfil de cliente
INSERT INTO clientes(id_usuario,tipo_cliente,razon_social,cuit_dni,telefono,direccion,estado_validacion,aprobado_corporativo)
SELECT id_usuario,'PARTICULAR',NULL,'30111226','1100000003','Av. Demo 456','VALIDADO',0
FROM usuarios u
WHERE u.nombre_usuario='cliente'
AND NOT EXISTS (SELECT 1 FROM clientes c WHERE c.id_usuario=u.id_usuario);

-- Paquete y envío de demostración exclusivo para el cliente
INSERT INTO paquetes(codigo,descripcion,peso_kg,tipo_mercaderia,es_fragil)
SELECT 'DEL-0005','Pedido de cliente demo',2.500,'Accesorios',0
WHERE NOT EXISTS (SELECT 1 FROM paquetes WHERE codigo='DEL-0005');

SET @cliente=(SELECT c.id_cliente FROM clientes c JOIN usuarios u ON u.id_usuario=c.id_usuario WHERE u.nombre_usuario='cliente' LIMIT 1);
SET @rem=(SELECT id_contacto FROM contactos WHERE tipo='REMITENTE' LIMIT 1);
SET @dest=(SELECT id_contacto FROM contactos WHERE tipo='DESTINATARIO' LIMIT 1);
SET @usr=(SELECT id_usuario FROM usuarios WHERE nombre_usuario='cliente' LIMIT 1);
SET @zona=(SELECT id_zona FROM zonas WHERE nombre='CABA' LIMIT 1);

INSERT INTO envios(codigo_envio,id_cliente,id_remitente,id_destinatario,id_paquete,estado,id_zona_destino,id_usuario_registro)
SELECT 'ENV-0005',@cliente,@rem,@dest,p.id_paquete,'REGISTRADO',@zona,@usr
FROM paquetes p
WHERE p.codigo='DEL-0005' AND @cliente IS NOT NULL
AND NOT EXISTS (SELECT 1 FROM envios WHERE codigo_envio='ENV-0005');
