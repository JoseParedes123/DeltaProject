# Guía de presentación y defensa - Delta Logística

## 1. ¿Qué es la aplicación?

Es una aplicación de escritorio desarrollada en Java para la gestión de operaciones de depósito de una empresa de logística.

La aplicación se conecta a MySQL mediante JDBC y permite consultar paquetes, aplicar filtros, cambiar estados, mover paquetes dentro del depósito y consultar la trazabilidad. Además, adapta las funciones disponibles según el rol del usuario.

## 2. Tecnologías

- Java
- JavaFX
- FXML para la interfaz
- CSS para estilos
- MySQL
- JDBC
- Maven para dependencias y ejecución
- Eclipse como IDE

## 3. Cómo está organizado el proyecto

```text
src/main/java/JavaApp/delta/
│
├── App.java
├── LoginController.java
├── DashboardController.java
│
├── dao/
│   ├── ConexionBD.java
│   ├── UsuarioDAO.java
│   └── PaqueteDAO.java
│
└── model/
    ├── Usuario.java
    ├── Paquete.java
    └── Ubicacion.java

src/main/resources/JavaApp/delta/
│
├── Login.fxml
├── Dashboard.fxml
└── style.css
```

### App.java
Es el punto de entrada de JavaFX. Crea la ventana principal, carga el Login y permite cambiar entre las pantallas FXML.

### LoginController.java
Controla el inicio de sesión. Toma usuario y contraseña, llama a `UsuarioDAO`, obtiene el usuario autenticado y luego abre el Dashboard.

### DashboardController.java
Es el controlador principal de la pantalla de gestión. Maneja búsquedas, filtros, permisos por rol, cambios de estado, movimientos, reparto, incidencias, historial y resumen administrativo.

### ConexionBD.java
Centraliza la conexión JDBC con MySQL.

### UsuarioDAO.java
Consulta la tabla `usuarios` y valida las credenciales mediante una consulta preparada.

### PaqueteDAO.java
Contiene las operaciones de acceso a datos relacionadas con paquetes, ubicaciones, movimientos, estados, historial, incidencias y consultas de resumen.

### model/
Son las clases que representan los datos que utiliza Java:
- `Usuario`
- `Paquete`
- `Ubicacion`

### FXML
Define visualmente las pantallas de JavaFX:
- `Login.fxml`: pantalla de ingreso.
- `Dashboard.fxml`: pantalla principal.
- `style.css`: apariencia visual.

## 4. Arquitectura que conviene explicar

El proyecto separa responsabilidades:

```text
INTERFAZ
FXML + JavaFX
      ↓
CONTROLADORES
LoginController / DashboardController
      ↓
ACCESO A DATOS
UsuarioDAO / PaqueteDAO
      ↓
CONEXIÓN
ConexionBD
      ↓
BASE DE DATOS
MySQL
```

Los modelos representan los datos que circulan entre estas capas.

## 5. Flujo del login

1. El usuario escribe usuario y contraseña.
2. `LoginController` recibe los datos.
3. Se llama a `UsuarioDAO.autenticar(...)`.
4. `UsuarioDAO` obtiene una conexión mediante `ConexionBD`.
5. Se consulta `usuarios` utilizando `PreparedStatement`.
6. La contraseña se compara con `SHA2(...,256)` en MySQL.
7. Si las credenciales son correctas, se crea un objeto `Usuario`.
8. Ese usuario queda como usuario actual.
9. Se carga `Dashboard.fxml`.
10. El Dashboard adapta la interfaz según el rol.

## 6. Roles

### ADMINISTRADOR
Puede acceder a las operaciones de depósito, reparto, incidencias, historial y resumen administrativo.

### ADMINISTRACION
Puede consultar información general, historial y resumen operativo, pero no modifica paquetes.

### OPERADOR_DEPOSITO
Puede:
- buscar paquetes;
- filtrar por estado;
- cambiar estados;
- mover paquetes;
- indicar el motivo de un movimiento;
- consultar historial.

### CHOFER
Puede:
- consultar los envíos asignados a sus rutas;
- actualizar estados de reparto;
- registrar incidencias;
- consultar historial.

### CLIENTE
Puede:
- consultar solamente sus propios envíos;
- ver estado y ubicación;
- consultar historial;
- no modificar información.

## 7. Ejemplo para explicar un movimiento de depósito

Cuando el operador selecciona un paquete y una ubicación:

1. Se comprueba que el usuario tenga permisos de depósito.
2. Se obtiene la ubicación actual.
3. Se desactiva el movimiento anterior.
4. Se libera la ubicación anterior.
5. Se ocupa la nueva ubicación.
6. Se registra un nuevo movimiento con usuario y motivo.
7. Se confirma la transacción.

Esto se realiza dentro de una transacción para evitar dejar la base en un estado intermedio si ocurre un error.

## 8. Ejemplo para explicar el historial

Cuando se consulta el historial de un paquete, `PaqueteDAO` combina información de:
- `movimientos_deposito`
- `historial`

Así se puede reconstruir qué ocurrió con el paquete y mostrarlo desde la aplicación.

## 9. Cómo demostrar el sistema

### Demostración recomendada

1. Iniciar sesión como `operador`.
2. Mostrar que aparecen las operaciones de depósito.
3. Seleccionar un paquete.
4. Cambiar su estado.
5. Mostrar que la tabla se actualiza.
6. Moverlo a otra ubicación e indicar un motivo.
7. Abrir `Ver historial`.
8. Cerrar sesión.
9. Iniciar como `chofer`.
10. Mostrar que solamente aparecen sus envíos asignados.
11. Cambiar el estado a `EN_REPARTO` o `ENTREGADO`.
12. Registrar una incidencia.
13. Cerrar sesión.
14. Iniciar como `cliente`.
15. Mostrar que solamente aparecen sus propios envíos.
16. Finalmente, mostrar `admin` y el resumen administrativo.

## 10. Preguntas que pueden hacer en la defensa

### ¿Por qué separaron DAO y Controller?
Porque el controlador maneja la interacción de la interfaz, mientras que el DAO concentra el acceso a la base de datos. Así se separan responsabilidades y el código es más fácil de mantener.

### ¿Por qué usan PreparedStatement?
Porque permite enviar parámetros de forma segura y evita construir consultas SQL concatenando directamente los datos introducidos por el usuario.

### ¿Qué hace JDBC?
Es la tecnología utilizada por Java para conectarse y comunicarse con MySQL.

### ¿Qué hace FXML?
Define la estructura visual de las pantallas JavaFX separándola del código que controla su comportamiento.

### ¿Cómo funcionan los roles?
Después del login se obtiene el rol del usuario. `DashboardController` determina qué paneles se muestran y qué operaciones puede ejecutar ese usuario.

### ¿Cómo evitan que un cliente vea paquetes de otro cliente?
La consulta de paquetes para el rol CLIENTE agrega la relación entre `clientes` y `envios` y filtra por el `id_usuario` del cliente autenticado.

### ¿Cómo sabe un chofer qué envíos puede ver?
La consulta relaciona el usuario con `choferes`, luego con `rutas` y finalmente con `envios`.

### ¿Cómo funciona la trazabilidad?
Los movimientos de depósito y los cambios de estado se registran en la base. La aplicación consulta esos registros y los presenta mediante `Ver historial`.

### ¿Qué ocurre si falla un movimiento?
El movimiento utiliza una transacción. Si una operación falla, se ejecuta `rollback()` para evitar que solamente una parte de los cambios quede guardada.

## 11. Qué NO conviene decir

No digan que JavaFX "hace la conexión con MySQL". La conexión la realiza JDBC mediante `ConexionBD`.

No digan que el FXML contiene la lógica del negocio. FXML define la interfaz; los controladores manejan las acciones.

No digan que los roles son solamente visuales. El controlador también comprueba el rol antes de ejecutar operaciones sensibles.

## 12. Nombres técnicos que se mantienen en inglés

Algunos nombres no se traducen porque pertenecen a Java, JavaFX o a convenciones de la plataforma, por ejemplo:

- `initialize()`: JavaFX lo busca automáticamente al cargar un FXML.
- `main()`: punto de entrada estándar de Java.
- `FXML`: tecnología de JavaFX.
- `DAO`: patrón de acceso a datos.
- `get...()` / `set...()`: convenciones de propiedades Java.
- `PreparedStatement`, `ResultSet`, `Connection`: clases de JDBC.
- nombres de columnas de la base de datos como `password_hash`, si así están definidos en el esquema.

Los nombres propios de las variables y métodos creados para la lógica del proyecto fueron llevados al español siempre que no dependieran de una API externa.
