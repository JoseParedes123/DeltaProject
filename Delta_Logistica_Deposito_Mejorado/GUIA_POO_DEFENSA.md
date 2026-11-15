# Guía para defender los cuatro pilares de POO

## 1. Encapsulamiento
**Dónde:** `src/main/java/JavaApp/delta/model/Paquete.java`, `Usuario.java`, `Ubicacion.java` y `Opcion.java`.

**Qué mostrar:** atributos declarados como `private` y sus métodos `get...()` / `set...()` (en `Opcion`, los atributos son `private final` y se consultan con getters).

**Cómo explicarlo:** “Los datos internos de cada objeto no se manipulan directamente desde otras clases. Se accede a ellos mediante métodos públicos, lo que permite controlar cómo se consultan o modifican”.

## 2. Herencia
**Dónde:** `src/main/java/JavaApp/delta/dao/BaseDAO.java`, `PaqueteDAO.java` y `UsuarioDAO.java`. También `App.java` hereda de `javafx.application.Application`.

**Qué mostrar:**
- `public class PaqueteDAO extends BaseDAO implements PaqueteRepositorio`
- `public class UsuarioDAO extends BaseDAO implements UsuarioRepositorio`
- En `BaseDAO`, el método `protected Connection obtenerConexion()`.

**Cómo explicarlo:** “PaqueteDAO y UsuarioDAO heredan de BaseDAO. Por eso reutilizan el método protegido para obtener la conexión a la base de datos, en lugar de repetir esa implementación en cada DAO. Además, App extiende Application para integrarse con JavaFX”.

## 3. Abstracción
**Dónde:** `PaqueteRepositorio.java` y `UsuarioRepositorio.java`.

**Qué mostrar:** las declaraciones de métodos, por ejemplo `buscar`, `mover`, `cambiarEstado` y `autenticar`. Las interfaces describen las operaciones disponibles, pero no contienen el SQL que las realiza.

**Cómo explicarlo:** “La interfaz muestra qué operaciones se pueden solicitar sin mostrar cómo se ejecutan. El controlador no necesita conocer las consultas SQL; solo conoce el contrato del repositorio”.

## 4. Polimorfismo
**Dónde:**
- `DashboardController.java`: el atributo `PaqueteRepositorio gestorPaquetes = new PaqueteDAO();`
- `AdminController.java`: el atributo `UsuarioRepositorio GestorUsuarios = new UsuarioDAO();`
- `LoginController.java`: la variable `UsuarioRepositorio repositorioUsuarios = new UsuarioDAO();`

**Cómo explicarlo:** “Una variable declarada con el tipo de una interfaz puede contener un objeto de una clase que implementa esa interfaz. El controlador invoca los métodos definidos por el contrato y Java ejecuta la implementación concreta del DAO. Así el controlador depende de la abstracción y no de la clase concreta”.

## Respuesta breve si preguntan por los cuatro pilares
“Encapsulamiento: en los modelos, con atributos privados y getters/setters. Herencia: PaqueteDAO y UsuarioDAO extienden BaseDAO y reutilizan la conexión; App también extiende Application. Abstracción: en las interfaces PaqueteRepositorio y UsuarioRepositorio, que separan las operaciones de su implementación SQL. Polimorfismo: en los controladores, que usan variables del tipo interfaz y reciben objetos concretos PaqueteDAO o UsuarioDAO”.

## Cómo comprobarlo en Eclipse
1. Importar el proyecto y ejecutar **Maven > Update Project** (o actualizar dependencias Maven).
2. Revisar que no haya errores en `Problems`.
3. Probar el inicio de sesión.
4. En Dashboard, probar búsqueda, cambio de estado, movimiento e historial; en Administración, probar listado y gestión de usuarios según los permisos disponibles.

La modificación organiza los DAO detrás de interfaces y una clase base; no cambia intencionalmente las consultas SQL ni las reglas de negocio.
