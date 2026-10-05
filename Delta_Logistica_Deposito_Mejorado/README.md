# Delta Logística - Aplicación de escritorio

## Qué incluye
- Login conectado a la tabla `usuarios`.
- Acceso para `OPERADOR_DEPOSITO` y `ADMINISTRADOR`.
- Búsqueda de paquetes por código, descripción o tipo de mercadería.
- Filtro por estado del envío.
- Visualización de ubicación actual.
- Cambio de estado del envío.
- Movimiento de un paquete a una ubicación libre.
- Registro del motivo de cada movimiento.
- Uso de transacciones para que mover un paquete actualice correctamente la ubicación anterior y la nueva.

## Base de datos
1. Importá `logistica_delta_bd_simplificada.sql` en MySQL/MariaDB (XAMPP/phpMyAdmin).
2. Ejecutá después `datos_demo.sql`.
3. Si tu MySQL tiene usuario/clave distintos de root sin contraseña, editá `ConexionBD.java`.

## Usuario de prueba
Usuario: operador
Contraseña: admin123

## Eclipse
Importá la carpeta como `Existing Maven Project`.
Luego:
- Project > Maven > Update Project.
- Esperá que Maven descargue JavaFX y MySQL Connector.
- Run As > Maven build...
- Goals: `clean javafx:run`

También se puede ejecutar desde una terminal ubicada en el proyecto con:
`mvn clean javafx:run`

## Importante
El proyecto usa Java 11 y JavaFX 13, igual que la base original. Si Eclipse usa otro JDK, seleccioná JDK 11 en Installed JREs y en el proyecto.

## Roles y pantallas de demostración

La aplicación Java ahora reconoce los cinco roles definidos por la base de datos:

- ADMINISTRADOR: acceso completo a operaciones de depósito, reparto, historial y resumen administrativo.
- ADMINISTRACION: consulta general y resumen operativo, sin modificar paquetes.
- OPERADOR_DEPOSITO: búsqueda, cambio de estado y movimientos de paquetes dentro del depósito.
- CHOFER: consulta de envíos asignados a sus rutas, actualización de estados de reparto y registro de incidencias.
- CLIENTE: consulta únicamente sus propios envíos y su historial.

Para probar los cinco roles con datos de demostración, ejecutar una vez `usuarios_roles_demo.sql` después de los scripts de creación y datos demo.

Usuarios demo:

- admin / admin123
- administracion / admin123
- operador / admin123
- chofer / chofer123
- cliente / cliente123
