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
