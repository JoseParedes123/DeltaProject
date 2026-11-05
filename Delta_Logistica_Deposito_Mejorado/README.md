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
1. Creá una base vacía llamada `logistica_delta` e importá `logistica_delta.sql`.
2. En la instalación nueva se crean las tablas compartidas con PHP y datos mínimos de práctica.
3. Configurá `DELTA_DB_URL`, `DELTA_DB_USER` y `DELTA_DB_PASSWORD` como variables de entorno o propiedades Java. Para usar el hosting, poné en `DELTA_DB_URL` la dirección JDBC y el nombre de la misma base que usa PHP.
4. Para conservar la base existente del hosting, usá una copia de seguridad y ejecutá una sola vez `database/migrar_host_s11.sql` desde el proyecto web; no importes `logistica_delta.sql` encima de la base con datos.

El SQL de práctica crea las cuentas `admin` / `DeltaAdmin123` y `operador` / `123456`. Cambiá estas claves antes de publicar una instalación nueva.

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

La base compartida define siete roles. Java admite los roles operativos y de cliente; `ATENCION_CLIENTE` se gestiona solamente desde el panel web. `SUPERADMIN` puede gestionar los roles elevados desde Java.

- ADMINISTRADOR: acceso completo a operaciones de depósito, reparto, historial y resumen administrativo.
- ADMINISTRACION: consulta general y resumen operativo, sin modificar paquetes.
- OPERADOR_DEPOSITO: búsqueda, cambio de estado y movimientos de paquetes dentro del depósito.
- CHOFER: consulta de envíos asignados a sus rutas, actualización de estados de reparto y registro de incidencias.
- CLIENTE: consulta únicamente sus propios envíos y su historial.


## Alta y cambio de rol de choferes
Al crear un usuario con rol `CHOFER` o cambiarle el rol a uno existente, el formulario pide licencia, categoría y vencimiento. La operación crea o actualiza su registro en `choferes` dentro de una transacción. Los choferes aparecen en la lista de asignación solo si el usuario está activo, la licencia no venció y el registro de chofer está en estado `ACTIVO`. No se requiere modificar el esquema de la base de datos.
