# API de Franquicias

API REST en **Spring Boot** para manejar franquicias. Una franquicia tiene un nombre y
un listado de sucursales; una sucursal tiene un nombre y un listado de productos; un
producto tiene un nombre y una cantidad de stock.

- Java 17, Spring Boot 3 (Spring Web + Spring Data JPA) y Gradle
- Persistencia en MySQL
- Empaquetado con Docker

## Cómo ejecutarlo en local

### Opción 1: con Docker (recomendada)

Solo necesitas tener Docker instalado. Este comando levanta MySQL y el API:

```bash
docker compose up --build
```

El API queda en `http://localhost:8080`. Para detenerlo: `docker compose down`.

### Opción 2: con Gradle

Necesitas Java 17 o superior y un MySQL corriendo en `localhost:3306` con usuario `root`
y clave `root` (la base de datos `franquicias` se crea sola).

```bash
./gradlew bootRun
```

En Windows usa `gradlew.bat` en lugar de `./gradlew`. No hace falta instalar Gradle:
el wrapper lo descarga la primera vez.

Si tu MySQL usa otros datos, cámbialos con variables de entorno:

```bash
DB_URL="jdbc:mysql://localhost:3306/franquicias" DB_USER="usuario" DB_PASSWORD="clave" ./gradlew bootRun
```

Las tablas se crean automáticamente al arrancar.

## Endpoints

| Método | Ruta | Qué hace |
| --- | --- | --- |
| POST | `/franquicias` | Agrega una franquicia |
| GET | `/franquicias` | Lista las franquicias |
| PUT | `/franquicias/{id}/nombre?nombre=...` | Actualiza el nombre de una franquicia |
| POST | `/franquicias/{id}/sucursales` | Agrega una sucursal a una franquicia |
| GET | `/franquicias/{id}/sucursales` | Lista las sucursales de una franquicia |
| PUT | `/sucursales/{id}/nombre?nombre=...` | Actualiza el nombre de una sucursal |
| POST | `/sucursales/{id}/productos` | Agrega un producto a una sucursal |
| DELETE | `/productos/{id}` | Elimina un producto |
| PUT | `/productos/{id}/stock?stock=...` | Modifica el stock de un producto |
| PUT | `/productos/{id}/nombre?nombre=...` | Actualiza el nombre de un producto |
| GET | `/franquicias/{id}/mayor-stock` | Producto con más stock de cada sucursal de la franquicia |

Si el id no existe, el API responde `404`. Si falta el nombre o el stock es negativo,
responde `400`.

## Ejemplos

```bash
# Crear una franquicia
curl -X POST http://localhost:8080/franquicias \
  -H "Content-Type: application/json" \
  -d '{"nombre": "La Esquina"}'

# Agregar una sucursal a la franquicia 1
curl -X POST http://localhost:8080/franquicias/1/sucursales \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Centro"}'

# Agregar productos a la sucursal 1
curl -X POST http://localhost:8080/sucursales/1/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Arepa", "stock": 10}'

curl -X POST http://localhost:8080/sucursales/1/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Pan", "stock": 40}'

# Modificar el stock del producto 1
curl -X PUT "http://localhost:8080/productos/1/stock?stock=99"

# Producto con más stock por sucursal de la franquicia 1
curl http://localhost:8080/franquicias/1/mayor-stock

# Eliminar el producto 1
curl -X DELETE http://localhost:8080/productos/1
```

Respuesta de `mayor-stock`:

```json
[
  { "sucursal": "Centro", "producto": "Arepa", "stock": 99 }
]
```

## Pruebas

```bash
./gradlew test
```

Las pruebas recorren los endpoints principales con una base de datos H2 en memoria, así que
no necesitan MySQL.

## Base de datos en la nube

El API lee la conexión de tres variables de entorno: `DB_URL`, `DB_USER` y
`DB_PASSWORD`. Para usar un MySQL en la nube (por ejemplo, AWS RDS) basta con
apuntarlas a esa base de datos; el código no cambia.

## Estructura del proyecto

```
src/main/java/com/prueba/franquicias
├── FranquiciasApplication.java   Arranque de la aplicación
├── controller                    Los endpoints
├── model                         Franquicia, Sucursal y Producto
└── repository                    Acceso a la base de datos
```

## Decisiones de diseño

- **Modelo simple:** cada sucursal guarda el id de su franquicia y cada producto el id
  de su sucursal, sin relaciones bidireccionales.
- **Consultas derivadas:** Spring Data genera las consultas a partir del nombre del
  método (por ejemplo, `findFirstBySucursalIdOrderByStockDesc`).
- **Un solo controlador:** el proyecto es pequeño, así que toda la lógica está en
  `FranquiciaController`.
