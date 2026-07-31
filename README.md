# 📦 Gestion de Stock Backend API

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white)
![Flyway](https://img.shields.io/badge/Flyway-10.20.1-CC0200?logo=flyway&logoColor=white)
![MinIO](https://img.shields.io/badge/MinIO-S3%20Compatible-C72E49?logo=minio&logoColor=white)
![Build](https://img.shields.io/badge/Maven-build-blue?logo=apachemaven&logoColor=white)

API REST moderne et multi-tenant de gestion de stock : catalogue d'articles, mouvements de stock, commandes clients/fournisseurs, ventes, clients, fournisseurs, entreprises et utilisateurs — avec authentification JWT, alertes de seuil de stock, notifications email et stockage de fichiers (photos) via MinIO.

---

## 📌 Description du projet

`gestion-stock-backend` (groupe Maven `cm.kfokam`) est le service backend d'une application de gestion de stock destinée à plusieurs entreprises (multi-tenant) : chaque utilisateur est rattaché à une `Entreprise`, et toutes les données métier (articles, clients, fournisseurs, commandes, ventes, mouvements de stock) sont isolées par entreprise. L'API expose des ressources REST classiques (CRUD), des règles métier de transition d'état pour les commandes, un calcul de stock réel avec alertes de seuil minimum, et un module de téléversement de fichiers pour les photos (clients, à ce jour).

---

## 🚀 Fonctionnalités principales

- **Authentification & autorisation** : JWT (access token + refresh token), rôles `ROLE_USER` / `ROLE_ADMIN` via Spring Security (`@PreAuthorize` sur chaque endpoint).
- **Multi-tenant** : chaque ressource est rattachée à une `Entreprise` ; l'inscription (`POST /api/v1/auth/register`) crée l'entreprise et son premier utilisateur administrateur.
- **Articles & Catégories** : catalogue avec code unique par entreprise, prix HT/TTC, TVA, seuil minimum de stock.
- **Mouvements de stock** : entrées, sorties, corrections positives/négatives, calcul du stock réel, endpoint d'alertes pour les articles sous le seuil minimum.
- **Commandes Clients & Fournisseurs** : cycle de vie avec transitions d'état contrôlées (`EN_PREPARATION → VALIDEE → LIVREE`/`ANNULEE`), déclenchement automatique de mouvements de stock à la livraison, historique par client/fournisseur.
- **Ventes** : enregistrement d'une vente avec sortie de stock associée.
- **Clients & Fournisseurs** : gestion des contacts avec adresse (composant `@Embeddable`), et upload de photo de profil pour les clients.
- **Entreprises & Utilisateurs** : gestion du tenant courant et de ses utilisateurs, changement de mot de passe self-service.
- **Notifications email** : confirmation de commande client et ordre de commande fournisseur, via Spring Mail + templates Thymeleaf.
- **Tableau de bord** : statistiques agrégées (ventes, stock, commandes) de l'entreprise courante.
- **Stockage de fichiers** : service `FileStorageService` générique (MinIO / S3 compatible), réutilisable pour tout futur besoin d'upload (photos article, logo entreprise, etc.).
- **Traçabilité JPA** : `created_at` / `updated_at` / `created_by` / `updated_by` sur toutes les entités (`AbstractEntity` + `@EnableJpaAuditing`).
- **Versionnement de schéma** : migrations Flyway (`V1` à `V3`) au lieu du DDL auto-généré par Hibernate (`ddl-auto: validate`).

---

## 🛠️ Stack technique

| Domaine | Technologies |
|---|---|
| **Langage & Framework** | Java 21, Spring Boot 3.2.5 |
| **Sécurité** | Spring Security, JWT (`jjwt` 0.12.3), rôles `ROLE_ADMIN` / `ROLE_USER` |
| **Persistance** | Spring Data JPA / Hibernate, PostgreSQL 15, Flyway 10.20.1 (versionnement de schéma) |
| **Stockage de fichiers** | MinIO Java SDK 8.5.7 (compatible S3) |
| **Email** | Spring Mail + Thymeleaf (templates HTML) |
| **Documentation API** | springdoc-openapi (Swagger UI) 2.5.0 |
| **Outillage** | Lombok, MapStruct 1.5.5.Final, Maven (via `mvnw`) |
| **Tests** | JUnit 5, Mockito, AssertJ, Spring Security Test |

> ℹ️ Le projet n'utilise ni MySQL ni H2 : la persistance repose exclusivement sur PostgreSQL, et la quasi-totalité de la suite de tests (`ArticleServiceImplTest`, `ClientControllerTest`, etc.) est constituée de tests unitaires/slice Mockito qui ne nécessitent aucune base de données. Seul `GestionStockApplicationTests` (chargement complet du contexte Spring) requiert une instance PostgreSQL et MinIO réelles (voir [Prérequis & Installation](#️-prérequis--installation)).

---

## 🏗️ Architecture & structure du projet

Organisation **par module métier** (package-by-feature) sous `cm.kfokam.stock` :

```
cm.kfokam.stock
├── article/            # Articles du catalogue (model, dto, mapper, service, controller, repository)
├── category/           # Catégories d'articles
├── client/             # Clients (+ upload de photo via FileStorageService)
├── fournisseur/        # Fournisseurs
├── commandeclient/     # Commandes client (+ lignes, états, historique)
├── commandefournisseur/# Commandes fournisseur (+ lignes, états, historique)
├── mvtstk/              # Mouvements de stock, stock réel, alertes
├── vente/               # Ventes (+ lignes de vente)
├── entreprise/          # Entreprise (tenant) + composant Adresse (historique)
├── utilisateur/         # Utilisateurs, rôles, changement de mot de passe
├── auth/                # JWT (JwtService, JwtAuthenticationFilter), CurrentUserService, AuthController
├── dashboard/           # Statistiques agrégées
├── email/               # Notifications email (Spring Mail + Thymeleaf)
├── storage/             # FileStorageService + implémentation MinIO
├── common/entity/       # AbstractEntity (audit JPA), Adresse (@Embeddable)
├── config/              # SecurityConfig, MinioConfig, SwaggerConfig, JpaAuditingConfig, ApplicationConfig
└── exception/           # Exceptions métier + GlobalExceptionHandler (@ControllerAdvice)
```

Chaque module métier suit la même convention interne :

- `model/` — entités JPA (héritent de `AbstractEntity` pour l'audit)
- `dto/` — **Java Records** en entrée (`XxxRequest`) et sortie (`XxxResponse`)
- `XxxMapper` — mapping DTO ↔ entité via **MapStruct**
- `XxxRepository` — `JpaRepository` Spring Data, scopé par entreprise
- `XxxService` / `XxxServiceImpl` — logique métier, transactionnelle
- `XxxController` — endpoints REST, sécurisés par `@PreAuthorize`, documentés en OpenAPI (`@Tag`, `@Operation`, `@ApiResponses`)

Les erreurs métier (ex. `EntityNotFoundException`, `DuplicateCodeException`, `StockInsuffisantException`, `InvalidStateTransitionException`, `FileStorageException`) sont centralisées dans `GlobalExceptionHandler` et traduites en réponses HTTP cohérentes (`ErrorResponse`).

---

## ⚙️ Prérequis & Installation

### Prérequis

- **JDK 21**
- **Maven 3.8+** (ou le wrapper `mvnw` / `mvnw.cmd` fourni, aucune installation requise)
- **Docker** (pour PostgreSQL et MinIO — un `docker-compose.yml` est fourni à la racine)
- Un IDE (IntelliJ IDEA, VS Code...) — optionnel

### Installation

```bash
# 1. Cloner le projet
git clone <URL_DU_REPO>
cd gestion-stock-backend

# 2. Démarrer PostgreSQL et MinIO (voir docker-compose.yml)
docker compose up -d

# 3. Compiler et lancer les tests
mvn clean install

# 4. Démarrer l'application
mvn spring-boot:run
```

Le `docker-compose.yml` fourni démarre :

| Service | Port hôte | Identifiants par défaut |
|---|---|---|
| PostgreSQL 15 | `5433` → 5432 | `postgres` / `postgrespassword`, base `gestion_stock_db` |
| MinIO | `9005` (API) / `9006` (console) | `minioadmin` / `minioadminpassword` |

Au premier démarrage, Flyway applique automatiquement les migrations (`src/main/resources/db/migration/V1__init_schema.sql`, `V2__add_adresse_columns.sql`, `V3__migrate_rue_to_adresse1_and_drop_rue.sql`) et le bucket MinIO (`gestion-stock-bucket`) est créé s'il n'existe pas.

---

## 🔧 Configuration de l'environnement (`application.yaml`)

La configuration se trouve dans `src/main/resources/application.yaml`. Extrait annoté des sections essentielles (valeurs de développement fournies par défaut — à surcharger via variables d'environnement ou un profil dédié pour un environnement de production) :

```yaml
server:
  port: 8080
  servlet:
    context-path: /api/v1        # préfixe unique appliqué à toutes les routes (ex: /api/v1/clients)

spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/gestion_stock_db
    username: postgres
    password: postgrespassword

  jpa:
    hibernate:
      ddl-auto: validate         # le schéma est piloté par Flyway, pas par Hibernate

  flyway:
    enabled: true
    locations: classpath:db/migration

# ── MinIO ──
minio:
  url: http://localhost:9005
  access-key: minioadmin
  secret-key: minioadminpassword
  bucket-name: gestion-stock-bucket

# ── JWT ──
application:
  security:
    jwt:
      secret-key: <clé secrète HMAC>
      expiration: 86400000          # 1 jour (ms)
      refresh-token:
        expiration: 604800000       # 7 jours (ms)
```

> ⚠️ Le dépôt contient des valeurs de développement en clair (clé JWT, mots de passe) destinées uniquement à un usage local. Ne les réutilisez jamais telles quelles en production.

---

## 🧪 Exécution des tests

```bash
mvn clean test
```

La suite couvre les services (Mockito), les contrôleurs (`@WebMvcTest` + `MockMvc`) et le module de stockage MinIO — sans dépendance à une base de données réelle. Seul `GestionStockApplicationTests` charge le contexte Spring complet et nécessite PostgreSQL + MinIO démarrés (`docker compose up -d`).

---

## 📖 Documentation API (Swagger UI)

Une fois l'application démarrée :

- **Swagger UI** : http://localhost:8080/api/v1/swagger-ui.html
- **Spécification OpenAPI (JSON)** : http://localhost:8080/api/v1/v3/api-docs

### S'authentifier dans Swagger UI

1. Obtenez un token via `POST /api/v1/auth/register` (première entreprise) ou `POST /api/v1/auth/authenticate`.
2. Copiez la valeur du champ `accessToken` de la réponse.
3. Dans Swagger UI, cliquez sur le bouton **Authorize** (cadenas en haut à droite).
4. Collez le token dans le champ `BearerAuth` (uniquement le token, sans le préfixe `Bearer`, Swagger l'ajoute automatiquement).
5. Cliquez sur **Authorize** puis **Close** : tous les appels suivants depuis l'interface incluront l'en-tête `Authorization: Bearer <token>`.

---

## 📬 Collection Postman

Aucune collection Postman statique n'est actuellement versionnée dans ce dépôt. Le moyen le plus rapide d'en obtenir une à jour :

1. Démarrer l'application.
2. Dans Postman : **File → Import → Link**, puis coller l'URL de la spécification OpenAPI : `http://localhost:8080/api/v1/v3/api-docs`.
3. Postman génère automatiquement une collection avec tous les endpoints, à jour avec le code.

---

## 👤 Auteur & Licence

Développé et maintenu par **gestion-stock-team**.

Aucune licence open-source n'est actuellement définie pour ce dépôt (usage interne / propriétaire par défaut).
