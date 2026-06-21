# Delivery Tracking API

Backend sistem za upravljanje i praćenje pošiljki. Omogućava kreiranje pošiljki, praćenje
statusa kroz ceo životni ciklus, uvid u kompletnu istoriju promena, masovni unos podataka
(CSV/Excel) i filtriranje pošiljki po više kriterijuma.

## Tehnološki stek

| Sloj | Tehnologija |
|------|-------------|
| Jezik / Runtime | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Build | Maven |
| Baza | PostgreSQL 16 |
| Migracije šeme | Liquibase |
| ORM | Spring Data JPA (Hibernate) |
| CSV import | Apache Commons CSV |
| Excel import | Apache POI |
| Kontejnerizacija | Docker + Docker Compose |

## Funkcionalnosti

- CRUD nad korisnicima i pošiljkama
- Jedinstveni tracking broj (auto-generisan iz baze preko sekvence)
- Životni ciklus statusa sa validacijom dozvoljenih prelaza
- Kompletna istorija promena statusa (vreme + napomena)
- Masovni unos preko CSV i Excel fajla (Strategy pattern, batch insert)
- Filtriranje pošiljki po korisniku, statusu i datumu kreiranja (JPA Specifications)
- Paginacija i sortiranje
- Globalno rukovanje greškama sa standardizovanim formatom odgovora

## Struktura projekta

```
com.rbt.delivery_tracking
├── entity/         JPA entiteti (User, Shipment, ShipmentStatusHistory)
├── enums/          ShipmentStatus
├── repository/     Spring Data JPA repozitorijumi
├── service/        Biznis logika
├── controller/     REST kontroleri
├── dto/            Request i Response objekti
├── specification/  Dinamičko filtriranje (ShipmentSpecifications)
├── importer/       Bulk import (Strategy pattern: CSV i Excel parseri)
└── exception/      Globalni exception handling
```

## Pokretanje

### Opcija 1 — Docker Compose

Ceo sistem (backend + PostgreSQL) se podiže **jednom komandom**, bez ručne konfiguracije.

1. Napravi `.env` fajl u korenu projekta sa sledećim sadržajem:

```dotenv
POSTGRES_DB=delivery_tracking
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
```

2. Pokreni:

```bash
docker compose up --build
```

Aplikacija je dostupna na `http://localhost:8081`.
PostgreSQL je izložen na `localhost:5433` (da ne dođe do konflikta sa lokalnom instalacijom na 5432).

Zaustavljanje:
```bash
docker compose down      # zaustavlja kontejnere, čuva podatke
docker compose down -v   # zaustavlja i briše podatke (volume)
```

### Opcija 2 — Lokalno (bez Dockera)

Preduslovi: instaliran JDK 21 i PostgreSQL.

1. Kreiraj bazu u PostgreSQL-u:
```sql
CREATE DATABASE delivery_tracking;
```

2. Postavi environment varijable (vidi sekciju ispod) ili koristi podrazumevane vrednosti.

3. Pokreni aplikaciju:
```bash
./mvnw spring-boot:run
```

Liquibase pri startu automatski kreira sve tabele.

## Environment varijable

Svi parametri se konfigurišu kroz environment varijable. Svaka ima podrazumevanu vrednost
(fallback) za lokalni razvoj.

| Varijabla | Opis | Podrazumevano |
|-----------|------|---------------|
| `SPRING_DATASOURCE_URL` | JDBC URL baze | `jdbc:postgresql://localhost:5432/delivery` |
| `SPRING_DATASOURCE_USERNAME` | Korisničko ime baze | `delivery_user` |
| `SPRING_DATASOURCE_PASSWORD` | Lozinka baze | `change_me` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate DDL režim (šemom upravlja Liquibase) | `validate` |
| `SPRING_LIQUIBASE_ENABLED` | Uključuje/isključuje Liquibase | `true` |
| `SERVER_PORT` | Port aplikacije | `8081` |
| `APP_IMPORT_MAX_FILE_SIZE_MB` | Maksimalna veličina fajla za import (MB) | `10` |

Dodatne varijable koje koristi `docker-compose.yml` (čita ih iz `.env`):

| Varijabla | Opis |
|-----------|------|
| `POSTGRES_DB` | Ime baze koju Docker kreira |
| `POSTGRES_USER` | Korisnik baze |
| `POSTGRES_PASSWORD` | Lozinka baze |

## Dokumentacija API-ja (Swagger)

Interaktivna OpenAPI dokumentacija je dostupna kad aplikacija radi:

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

Kroz Swagger UI mogu se videti svi endpoint-i i poslati zahtevi direktno iz browsera
("Try it out").

## REST API

Bazni prefiks: `/api/v1`. Format tela: `application/json` (osim importa: `multipart/form-data`).

### Korisnici

| Metoda | Ruta | Opis |
|--------|------|------|
| `POST` | `/api/v1/users` | Kreira korisnika |
| `GET` | `/api/v1/users` | Lista korisnika (paginacija) |
| `GET` | `/api/v1/users/{id}` | Detalji korisnika |

Primer kreiranja korisnika:
```json
POST /api/v1/users
{
  "fullName": "Marko Markovic",
  "email": "marko@example.com",
  "phone": "+381641234567"
}
```

### Pošiljke

| Metoda | Ruta | Opis |
|--------|------|------|
| `POST` | `/api/v1/shipments` | Kreira pošiljku (status `CREATED` + prvi history zapis) |
| `GET` | `/api/v1/shipments` | Lista pošiljki sa filterima i paginacijom |
| `GET` | `/api/v1/shipments/{id}` | Detalji pošiljke |
| `GET` | `/api/v1/shipments/by-tracking/{trackingNumber}` | Pretraga po tracking broju |
| `PUT` | `/api/v1/shipments/{id}` | Ažurira opis pošiljke |
| `DELETE` | `/api/v1/shipments/{id}` | Briše pošiljku (i njenu istoriju) |
| `PATCH` | `/api/v1/shipments/{id}/status` | Menja status i upisuje istoriju |
| `GET` | `/api/v1/shipments/{id}/history` | Istorija promena statusa |
| `POST` | `/api/v1/shipments/import` | Masovni unos iz CSV/Excel fajla |

Primer kreiranja pošiljke (tracking broj se generiše automatski):
```json
POST /api/v1/shipments
{
  "userId": 1,
  "description": "Laptop Dell XPS 15"
}
```

Primer promene statusa:
```json
PATCH /api/v1/shipments/1/status
{
  "status": "IN_TRANSIT",
  "note": "Pošiljka preuzeta u sortirnom centru"
}
```

### Filtriranje pošiljki

`GET /api/v1/shipments` podržava sledeće opcione query parametre (kombinuju se AND logikom):

| Parametar | Tip | Primer |
|-----------|-----|--------|
| `userId` | Long | `?userId=1` |
| `status` | enum | `?status=IN_TRANSIT` |
| `createdFrom` | ISO datetime | `?createdFrom=2026-06-01T00:00:00` |
| `createdTo` | ISO datetime | `?createdTo=2026-06-30T23:59:59` |
| `page` | int | `?page=0` |
| `size` | int | `?size=20` |
| `sort` | string | `?sort=createdAt,desc` |

Primer:
```
GET /api/v1/shipments?userId=1&status=IN_TRANSIT&page=0&size=20&sort=createdAt,desc
```

### Statusi pošiljke

```
CREATED → PICKED_UP → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED
                                                     ↘ CANCELLED
```

`DELIVERED` i `CANCELLED` su terminalni statusi (nema daljih promena). Nedozvoljen prelaz
vraća `409 Conflict`.

## Masovni unos (CSV / Excel)

`POST /api/v1/shipments/import` prima `multipart/form-data` sa poljem `file`.
Tip fajla se detektuje po ekstenziji (`.csv` ili `.xlsx`).

Očekivane kolone:

| Kolona | Obavezno | Opis |
|--------|----------|------|
| `userId` | da | ID postojećeg korisnika |
| `description` | ne | Opis pošiljke |
| `status` | ne | Početni status (prazan → `CREATED`) |

Primer CSV-a:
```csv
userId,description,status
1,Laptop Dell XPS,CREATED
1,Knjige,IN_TRANSIT
2,Telefon,
```

Odgovor sadrži izveštaj o uvozu — validni redovi se snimaju, nevalidni se preskaču i
prijavljuju sa brojem reda i razlogom:
```json
{
  "totalRows": 3,
  "imported": 2,
  "failed": 1,
  "errors": [
    { "row": 4, "message": "User with id=2 not found" }
  ]
}
```

## Model baze

```
app_user (1) ──< (N) shipment (1) ──< (N) shipment_status_history
```

- `app_user` — korisnici
- `shipment` — pošiljke (FK na `app_user`, jedinstven `tracking_number`)
- `shipment_status_history` — istorija promena statusa (FK na `shipment`, kaskadno brisanje)

Šemom upravlja isključivo Liquibase (`src/main/resources/db/changelog`). Hibernate je
podešen na `ddl-auto=validate` i ne menja šemu.

## Format greške

Sve greške vraćaju standardizovan format:
```json
{
  "timestamp": "2026-06-20T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Shipment with id=99 not found",
  "path": "/api/v1/shipments/99"
}
```

## Testovi

Projekat sadrži unit testove za ključnu biznis logiku (JUnit 5 + Mockito), bez zavisnosti
od baze, pa se pokreću svuda bez dodatne infrastrukture:

- `ShipmentStatusTest` — pravila prelaza statusa (dozvoljeni/nedozvoljeni prelazi, terminalni statusi)
- `ShipmentServiceTest` — kreiranje pošiljke, promena statusa, upis istorije, validacija
- `UserServiceTest` — kreiranje korisnika, duplikat email-a, nepostojeći korisnik
- `ImportServiceTest` — validacija reda po reda i izveštaj o uvozu (preskakanje nevalidnih redova)

Pokretanje:
```bash
./mvnw test
```
