-- DATOS DE PRUEBA PARA PRESENTAR LA GESTION DE DEPOSITO
-- Ejecutar DESPUES de logistica_delta_bd_simplificada.sql
USE logistica_delta;

INSERT INTO usuarios(nombre,apellido,dni,email,nombre_usuario,password_hash,rol)
VALUES ('Juan','Pérez','30111222','juan.perez@delta.local','operador',SHA2('admin123',256),'OPERADOR_DEPOSITO')
ON DUPLICATE KEY UPDATE nombre_usuario=nombre_usuario;

INSERT INTO zonas(nombre,cobertura,descripcion)
VALUES ('CABA','CABA_GBA','Cobertura CABA')
ON DUPLICATE KEY UPDATE nombre=nombre;

INSERT INTO depositos(nombre,direccion)
SELECT 'Central Delta','Av. Logística 123, Buenos Aires'
WHERE NOT EXISTS (SELECT 1 FROM depositos WHERE nombre='Central Delta');

INSERT INTO ubicaciones(id_deposito,sector,estante,posicion)
SELECT d.id_deposito,'A','01','01' FROM depositos d WHERE d.nombre='Central Delta'
AND NOT EXISTS (SELECT 1 FROM ubicaciones u WHERE u.id_deposito=d.id_deposito AND u.sector='A' AND u.estante='01' AND u.posicion='01');
INSERT INTO ubicaciones(id_deposito,sector,estante,posicion)
SELECT d.id_deposito,'A','01','02' FROM depositos d WHERE d.nombre='Central Delta'
AND NOT EXISTS (SELECT 1 FROM ubicaciones u WHERE u.id_deposito=d.id_deposito AND u.sector='A' AND u.estante='01' AND u.posicion='02');
INSERT INTO ubicaciones(id_deposito,sector,estante,posicion)
SELECT d.id_deposito,'B','02','01' FROM depositos d WHERE d.nombre='Central Delta'
AND NOT EXISTS (SELECT 1 FROM ubicaciones u WHERE u.id_deposito=d.id_deposito AND u.sector='B' AND u.estante='02' AND u.posicion='01');
INSERT INTO ubicaciones(id_deposito,sector,estante,posicion)
SELECT d.id_deposito,'B','02','02' FROM depositos d WHERE d.nombre='Central Delta'
AND NOT EXISTS (SELECT 1 FROM ubicaciones u WHERE u.id_deposito=d.id_deposito AND u.sector='B' AND u.estante='02' AND u.posicion='02');
INSERT INTO ubicaciones(id_deposito,sector,estante,posicion)
SELECT d.id_deposito,'C','03','01' FROM depositos d WHERE d.nombre='Central Delta'
AND NOT EXISTS (SELECT 1 FROM ubicaciones u WHERE u.id_deposito=d.id_deposito AND u.sector='C' AND u.estante='03' AND u.posicion='01');

INSERT INTO clientes(id_usuario,tipo_cliente,razon_social,cuit_dni,telefono,direccion,estado_validacion,aprobado_corporativo)
SELECT id_usuario,'CORPORATIVO','Delta Demo','30-00000000-0','1100000000','Av. Logística 123','VALIDADO',1
FROM usuarios WHERE nombre_usuario='operador'
AND NOT EXISTS (SELECT 1 FROM clientes c JOIN usuarios u ON u.id_usuario=c.id_usuario WHERE u.nombre_usuario='operador');

INSERT INTO contactos(tipo,nombre,telefono,email,direccion,id_zona,id_cliente)
SELECT 'REMITENTE','Cliente Demo','1100000001','remitente@delta.local','CABA',z.id_zona,c.id_cliente
FROM zonas z, clientes c JOIN usuarios u ON u.id_usuario=c.id_usuario
WHERE z.nombre='CABA' AND u.nombre_usuario='operador'
AND NOT EXISTS (SELECT 1 FROM contactos WHERE nombre='Cliente Demo' AND tipo='REMITENTE');

INSERT INTO contactos(tipo,nombre,telefono,email,direccion,id_zona)
SELECT 'DESTINATARIO','María González','1100000002','maria@delta.local','CABA',z.id_zona
FROM zonas z WHERE z.nombre='CABA'
AND NOT EXISTS (SELECT 1 FROM contactos WHERE nombre='María González' AND tipo='DESTINATARIO');

INSERT INTO paquetes(codigo,descripcion,peso_kg,tipo_mercaderia,es_fragil)
SELECT 'DEL-0001','Caja de repuestos',8.500,'Repuestos',0
WHERE NOT EXISTS (SELECT 1 FROM paquetes WHERE codigo='DEL-0001');
INSERT INTO paquetes(codigo,descripcion,peso_kg,tipo_mercaderia,es_fragil)
SELECT 'DEL-0002','Monitor profesional',4.200,'Electrónica',1
WHERE NOT EXISTS (SELECT 1 FROM paquetes WHERE codigo='DEL-0002');
INSERT INTO paquetes(codigo,descripcion,peso_kg,tipo_mercaderia,es_fragil)
SELECT 'DEL-0003','Documentación empresarial',1.100,'Documentos',0
WHERE NOT EXISTS (SELECT 1 FROM paquetes WHERE codigo='DEL-0003');
INSERT INTO paquetes(codigo,descripcion,peso_kg,tipo_mercaderia,es_fragil)
SELECT 'DEL-0004','Equipamiento informático',12.700,'Electrónica',1
WHERE NOT EXISTS (SELECT 1 FROM paquetes WHERE codigo='DEL-0004');

SET @zona=(SELECT id_zona FROM zonas WHERE nombre='CABA');
SET @cliente=(SELECT c.id_cliente FROM clientes c JOIN usuarios u ON u.id_usuario=c.id_usuario WHERE u.nombre_usuario='operador');
SET @rem=(SELECT id_contacto FROM contactos WHERE tipo='REMITENTE' AND nombre='Cliente Demo' LIMIT 1);
SET @dest=(SELECT id_contacto FROM contactos WHERE tipo='DESTINATARIO' AND nombre='María González' LIMIT 1);
SET @usr=(SELECT id_usuario FROM usuarios WHERE nombre_usuario='operador');

INSERT INTO envios(codigo_envio,id_cliente,id_remitente,id_destinatario,id_paquete,estado,id_zona_destino,id_usuario_registro)
SELECT 'ENV-0001',@cliente,@rem,@dest,id_paquete,'RECIBIDO_DEPOSITO',@zona,@usr FROM paquetes WHERE codigo='DEL-0001'
AND NOT EXISTS (SELECT 1 FROM envios WHERE codigo_envio='ENV-0001');
INSERT INTO envios(codigo_envio,id_cliente,id_remitente,id_destinatario,id_paquete,estado,id_zona_destino,id_usuario_registro)
SELECT 'ENV-0002',@cliente,@rem,@dest,id_paquete,'PREPARADO',@zona,@usr FROM paquetes WHERE codigo='DEL-0002'
AND NOT EXISTS (SELECT 1 FROM envios WHERE codigo_envio='ENV-0002');
INSERT INTO envios(codigo_envio,id_cliente,id_remitente,id_destinatario,id_paquete,estado,id_zona_destino,id_usuario_registro)
SELECT 'ENV-0003',@cliente,@rem,@dest,id_paquete,'EN_TRANSITO',@zona,@usr FROM paquetes WHERE codigo='DEL-0003'
AND NOT EXISTS (SELECT 1 FROM envios WHERE codigo_envio='ENV-0003');
INSERT INTO envios(codigo_envio,id_cliente,id_remitente,id_destinatario,id_paquete,estado,id_zona_destino,id_usuario_registro)
SELECT 'ENV-0004',@cliente,@rem,@dest,id_paquete,'EXTRAVIADO',@zona,@usr FROM paquetes WHERE codigo='DEL-0004'
AND NOT EXISTS (SELECT 1 FROM envios WHERE codigo_envio='ENV-0004');

SET @dep=(SELECT id_deposito FROM depositos WHERE nombre='Central Delta');
SET @u1=(SELECT id_ubicacion FROM ubicaciones WHERE id_deposito=@dep AND sector='A' AND estante='01' AND posicion='01');
SET @u2=(SELECT id_ubicacion FROM ubicaciones WHERE id_deposito=@dep AND sector='A' AND estante='01' AND posicion='02');
SET @u3=(SELECT id_ubicacion FROM ubicaciones WHERE id_deposito=@dep AND sector='B' AND estante='02' AND posicion='01');

INSERT INTO movimientos_deposito(id_paquete,id_ubicacion_nueva,es_actual,id_usuario,motivo)
SELECT id_paquete,@u1,1,@usr,'Recepción en depósito central' FROM paquetes WHERE codigo='DEL-0001'
AND NOT EXISTS (SELECT 1 FROM movimientos_deposito md WHERE md.id_paquete=paquetes.id_paquete);
INSERT INTO movimientos_deposito(id_paquete,id_ubicacion_nueva,es_actual,id_usuario,motivo)
SELECT id_paquete,@u2,1,@usr,'Clasificación de paquete frágil' FROM paquetes WHERE codigo='DEL-0002'
AND NOT EXISTS (SELECT 1 FROM movimientos_deposito md WHERE md.id_paquete=paquetes.id_paquete);
INSERT INTO movimientos_deposito(id_paquete,id_ubicacion_nueva,es_actual,id_usuario,motivo)
SELECT id_paquete,@u3,1,@usr,'Ingreso y clasificación' FROM paquetes WHERE codigo='DEL-0004'
AND NOT EXISTS (SELECT 1 FROM movimientos_deposito md WHERE md.id_paquete=paquetes.id_paquete);

UPDATE ubicaciones SET ocupada=1 WHERE id_ubicacion IN (@u1,@u2,@u3);
