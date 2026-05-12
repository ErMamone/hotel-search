# Hotel Search Service

Servicio REST para búsquedas de disponibilidad hotelera.
Recibe búsquedas vía HTTP, las publica en Kafka, las persiste vía consumer en PostgreSQL, y permite contar búsquedas equivalentes.

Spring Boot 3.4 · Java 21 · Kafka · PostgreSQL 16 · Arquitectura hexagonal.

---

## Requisitos

- Docker y Docker Compose instalados.
- Puerto `8080` libre en el host.

No se requiere Java, Maven, ni ninguna otra herramienta instalada localmente — el build se realiza dentro del contenedor.

---

## Cómo levantar

Desde la raíz del proyecto:

```bash
docker compose up --build
```

Esperar hasta ver:

```
hotel-search-app  | Started HotelSearchApplication in X.XXX seconds
```

Para detener:

```bash
docker compose down
```

Para limpiar volúmenes (reset total de datos):

```bash
docker compose down -v
```

---

## Endpoints

Swagger UI disponible en: **http://localhost:8080/swagger-ui.html**

### POST `/search`

Registra una búsqueda y devuelve un identificador determinístico.

**Request:**

```bash
curl -X POST http://localhost:8080/search \
  -H "Content-Type: application/json" \
  -d '{
    "hotelId": "1234aBc",
    "checkIn": "29/12/2025",
    "checkOut": "31/12/2025",
    "ages": [30, 29, 1, 3]
  }'
```

**Response 201:**

```json
{
	"searchId": "a3f5b8c9d2e7..."
}
```

### GET `/count?searchId={searchId}`

Devuelve la cantidad de búsquedas equivalentes registradas para un `searchId`.

**Request:**

```bash
curl "http://localhost:8080/count?searchId=a3f5b8c9d2e7..."
```

**Response 200:**

```json
{
	"searchId": "a3f5b8c9d2e7...",
	"search": {
		"hotelId": "1234aBc",
		"checkIn": "29/12/2025",
		"checkOut": "31/12/2025",
	    "ages": [30, 29, 1, 3]
	},
	"count": 1
}
```

---

## Validaciones y manejo de errores

| Caso                              | Status | Body                                                    |
|-----------------------------------|--------|---------------------------------------------------------|
| Payload válido                    | 201    | `{"searchId": "..."}`                                   |
| `hotelId` vacío o no alfanumérico | 400    | `{"errors": {"hotelId": "..."}}`                        |
| `ages` vacío o null               | 400    | `{"errors": {"ages": "..."}}`                           |
| `checkIn` >= `checkOut`           | 400    | `{"error": "checkIn must be before checkOut"}`          |
| `searchId` no encontrado          | 404    | `{"error": "Search not found: ..."}`                    |
| Falla publicando a Kafka          | 503    | `{"error": "Event publishing failed, try again later"}` |
| Error inesperado                  | 500    | `{"error": "Unexpected error"}`                         |

---

## Comportamiento del searchId

El `searchId` se genera de forma **determinística** mediante SHA-256 sobre la concatenación normalizada de `hotelId | checkIn | checkOut | ages`, sin acceso a la base de datos.

**El orden de `ages` afecta al `searchId`**, según lo especificado en el enunciado: dos búsquedas con las mismas edades en distinto orden son consideradas búsquedas distintas y producen `searchId` distintos.

---

## Probar el flujo completo

```bash
# 1. Hacer una búsqueda
SEARCH_ID=$(curl -s -X POST http://localhost:8080/search \
  -H "Content-Type: application/json" \
  -d '{"hotelId":"hotelA","checkIn":"01/01/2025","checkOut":"05/01/2025","ages":[30,29,1]}' \
  | grep -o '"searchId":"[^"]*"' | cut -d'"' -f4)

echo "searchId: $SEARCH_ID"

# 2. Repetir la misma búsqueda 2 veces más
curl -X POST http://localhost:8080/search \
  -H "Content-Type: application/json" \
  -d '{"hotelId":"hotelA","checkIn":"01/01/2025","checkOut":"05/01/2025","ages":[30,29,1]}'

curl -X POST http://localhost:8080/search \
  -H "Content-Type: application/json" \
  -d '{"hotelId":"hotelA","checkIn":"01/01/2025","checkOut":"05/01/2025","ages":[30,29,1]}'

# 3. Esperar a que el consumer procese y consultar el count
sleep 2
curl "http://localhost:8080/count?searchId=$SEARCH_ID"
# Debería devolver count: 3
```

---

## Tests

```bash
mvn clean verify
```

El build incluye **Jacoco con check al 80%** sobre líneas, branches y métodos. Si la cobertura cae por debajo, el build falla.

Los tests `HotelSearchPersistenceAdapterTest` y `KafkaSearchFlowIntegrationTest` utilizan **Testcontainers** y requieren Docker accesible mediante el socket por defecto. Para entornos sin Docker:

```bash
mvn clean verify -Dtest='!HotelSearchPersistenceAdapterTest,!KafkaSearchFlowIntegrationTest'
```

---

## Arquitectura y decisiones de diseño

### Arquitectura

Hexagonal estricta (ports & adapters). El dominio no depende de Spring, JPA, Kafka, ni REST. La adaptación se realiza en `infrastructure/adapter/*`.

- **`domain/`** — Núcleo: modelos, puertos, servicios. Sin dependencias de frameworks.
	- `model/` — Records inmutables.
	- `port/in/` — Use cases.
	- `port/out/` — Repository, EventPublisher.
	- `service/` — Implementaciones de use cases (POJOs).
	- `exception/` — Excepciones de dominio.
- **`application/mapper/`** — Mapeo Domain ↔ DTO REST.
- **`infrastructure/`** — Adaptadores y configuración Spring.
	- `adapter/in/rest/` — Controller, DTOs, exception handler.
	- `adapter/out/kafka/` — Publisher, Consumer.
	- `adapter/out/persistence/` — JPA Entity, Repository, Adapter.
	- `config/` — Spring configs, properties tipadas.

### Decisiones técnicas

- **Inmutabilidad estricta:** records con `List.copyOf()` en compact constructors.
- **searchId determinístico:** SHA-256 sobre payload normalizado, sin acceso a DB.
- **Profiles separados:** `local` (app desde IDE, infra mapeada al host) y `docker` (network interna del compose).
- **`@Transactional` con `readOnly=true`** en métodos de lectura del adapter de persistencia.
- **Publisher síncrono con timeout** de 5 segundos: el cliente recibe 503 si Kafka no responde, evitando silent data loss.

---

## Estructura del proyecto

- `pom.xml` — Configuración Maven, dependencias, Jacoco.
- `Dockerfile` — Build multi-stage con Maven y Eclipse Temurin 21.
- `docker-compose.yml` — Orquesta Postgres, Kafka y la app.
- `README.md` — Este archivo.
- `src/main/java/org/meroz/` — Código fuente (dominio, application, infrastructure).
- `src/main/resources/` — `application.yml` base + perfiles `local` y `docker`.
- `src/test/java/org/meroz/` — Tests unitarios y de integración.
