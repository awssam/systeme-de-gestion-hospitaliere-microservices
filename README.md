# Système de Gestion Hospitalière - Architecture Microservices

## Vue d'ensemble

Application de gestion hospitalière basée sur une architecture microservices avec Spring Boot 4.x et Spring Cloud 2025.x.

## Architecture

```
                    ┌──────────────┐
                    │ Config Server│ :8888
                    └──────┬───────┘
                           │
┌─────────┐    ┌───────────┴───────────┐    ┌──────────────┐
│  Client  │───▶│     API Gateway      │───▶│Eureka Server │ :8761
└─────────┘    │       :8080           │    └──────────────┘
               └───┬──────┬──────┬────┘
                   │      │      │
          ┌────────┘      │      └────────┐
          ▼               ▼               ▼
   ┌──────────┐   ┌──────────────┐  ┌──────────────┐
   │ Patient  │   │ Appointment  │  │Medical Record│
   │ Service  │   │   Service    │  │   Service    │
   │  :8081   │   │    :8082     │  │    :8083     │
   └──────────┘   └──────────────┘  └──────────────┘
       H2 DB          H2 DB            H2 DB
```

## Services

| Service                | Port | Description                                 |
| ---------------------- | ---- | ------------------------------------------- |
| Config Server          | 8888 | Configuration centralisée (profil `native`) |
| Eureka Server          | 8761 | Annuaire de services                        |
| API Gateway            | 8080 | Point d'entrée unique, routage MVC          |
| Patient Service        | 8081 | CRUD des patients                           |
| Appointment Service    | 8082 | Gestion des rendez-vous (Feign → Patient)   |
| Medical Record Service | 8083 | Dossiers médicaux (Feign → Patient)         |

## Technologies

- **Spring Boot 4.0.3** / **Spring Cloud 2025.1.0**
- **Spring Cloud Gateway MVC** (Tomcat, `HandlerFunctions` DSL)
- **Spring Cloud Netflix Eureka** (découverte de services)
- **Spring Cloud Config** (configuration centralisée)
- **OpenFeign** (communication inter-services)
- **Resilience4j** (Circuit Breaker, Retry, Fallback)
- **H2** (base de données in-memory)
- **Spring Data JPA / Hibernate**

## Lancement

### Prérequis

- Java 21+
- Maven 3.9+ (ou Docker avec `maven:3.9-eclipse-temurin-17`)

### Compilation

```bash
# Via Docker
docker run --rm -v ~/.m2:/root/.m2 -v $(pwd):/app -w /app \
  maven:3.9-eclipse-temurin-17 mvn clean install -DskipTests
```

### Démarrage (ordre important)

```bash
# 1. Config Server
java -jar config-server/target/config-server-0.0.1-SNAPSHOT.jar &
sleep 15

# 2. Eureka Server
java -jar eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar &
sleep 15

# 3. Microservices métiers + Gateway
java -jar patient-service/target/patient-service-0.0.1-SNAPSHOT.jar &
java -jar appointment-service/target/appointment-service-0.0.1-SNAPSHOT.jar &
java -jar medical-record-service/target/medical-record-service-0.0.1-SNAPSHOT.jar &
java -jar api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar &
sleep 20
```

## Endpoints API (via Gateway :8080)

### Patients

```bash
GET    /api/patients           # Liste des patients
GET    /api/patients/{id}      # Patient par ID
POST   /api/patients           # Créer un patient
```

### Rendez-vous

```bash
GET    /api/appointments              # Liste des RDV
GET    /api/appointments/patient/{id} # RDV par patient
POST   /api/appointments              # Créer un RDV
```

### Dossiers médicaux

```bash
GET    /api/medical-records              # Liste des dossiers
GET    /api/medical-records/patient/{id} # Dossiers par patient
POST   /api/medical-records              # Créer un dossier
```

## Test rapide

```bash
# Créer un patient
curl -X POST http://localhost:8080/api/patients \
  -H "Content-Type: application/json" \
  -d '{"nom":"Doe","prenom":"John","contact":"0102030405","dateNaissance":"1990-01-01"}'

# Créer un rendez-vous
curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -d '{"patientId":1,"date":"2026-03-01T10:00:00"}'

# Créer un dossier médical
curl -X POST http://localhost:8080/api/medical-records \
  -H "Content-Type: application/json" \
  -d '{"patientId":1,"diagnostics":"Examen de routine"}'
```

## Résilience (Circuit Breaker)

Lorsque le Patient Service est indisponible, les services `appointment-service` et `medical-record-service` déclenchent un **fallback Resilience4j** qui retourne un message contrôlé au lieu d'une erreur non gérée.

Configuration : `slidingWindowSize=10`, `failureRateThreshold=50%`, `retry maxAttempts=3`.
