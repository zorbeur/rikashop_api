# Job Platform API
![WhatsApp Image 2025-10-22 à 18 54 12_de55bf90](https://github.com/user-attachments/assets/dc3c242f-3bed-42c0-b12c-682b03e7283e)

API Spring Boot complète pour une plateforme d’offres d’emploi (authentification, rôles, offres, candidatures) avec rendu Thymeleaf pour quelques vues web.

## Fonctionnalités
- **[Authentification]** Inscription, connexion, refresh token (JWT HS256)
- **[Gestion rôles]** `CANDIDAT`, `RECRUTEUR`, `ADMIN`
- **[Vérification email]** Code à 8 chiffres (envoi email activable)
- **[Offres d’emploi]** CRUD côté recruteur, listing public
- **[Candidatures]** Côté candidat (WIP selon besoins)
- **[Vues web]** Pages d’auth + dashboards simples en Thymeleaf
- **[OpenAPI]** Swagger UI

## Pile technique
- **Back-end**: Spring Boot 3, Spring Security 6, Spring Data JPA (Hibernate)
- **Base de données**: MySQL
- **Auth**: JWT (jjwt 0.11.x)
- **Vues**: Thymeleaf

## Prérequis
- Java 17+
- Maven 3.9+
- MySQL 8 (ou Docker MySQL)

## Installation
1. Cloner le dépôt
```
git clone https://github.com/zorbeur/rikashop_api.git
cd rikashop-cursor-build-a-complete-job-platform-api-0336
```
2. Configurer la base de données MySQL (un schéma `job_platform` doit exister)
3. Copier/adapter `src/main/resources/application.properties`

## Configuration
Fichier: `src/main/resources/application.properties`

- **Serveur**
  - `server.port=8080`
- **Base de données**
  - `spring.datasource.url=jdbc:mysql://localhost:3306/job_platform?...`
  - `spring.datasource.username` / `spring.datasource.password`
  - `spring.jpa.hibernate.ddl-auto=create` (à mettre sur `update`/`validate` en prod)
- **JWT**
  - `app.jwt.secret=${APP_JWT_SECRET:change-this-super-secret-jwt-key}`
    - Accepte Base64, Base64URL ou texte brut
    - Doit faire au moins 32 octets (256 bits) pour HS256
  - `app.jwt.expiration=86400000` (ms)
- **Email** (désactivé par défaut en local)
  - `app.mail.enabled=false`
  - `spring.mail.*` (Gmail SMTP si activé; utilisez un App Password en prod)
- **Thymeleaf**
  - `spring.thymeleaf.cache=false` (utile en dev)
- **OpenAPI**
  - `springdoc.api-docs.path=/v3/api-docs`
  - `springdoc.swagger-ui.path=/swagger-ui.html`

Variables d’environnement courantes:
- `APP_JWT_SECRET` (recommandé en prod)
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`

## Lancer l’application
```
mvn spring-boot:run
```
Puis ouvrir:
- Swagger UI: http://localhost:8080/swagger-ui.html
- Page d’accueil: http://localhost:8080/

## Endpoints principaux (extrait)
- **[Auth]**
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `POST /api/auth/refresh`
- **[Offres]**
  - `GET /offers` (SSR Thymeleaf)
  - `GET /api/offers/**` (API publique)
- **[Dashboards]**
  - `GET /dashboard/candidate`
  - `GET /dashboard/recruiter`
  - `GET /dashboard/admin`

## Sécurité JWT
- **[Algo]** HS256 avec clé secrète d’au moins 32 octets.
- **[Claims]** `sub` = email, `role` = rôle utilisateur.
- **[Expiration]** configurée via `app.jwt.expiration`.

## Email
- **[Local/dev]** `app.mail.enabled=false` -> les emails sont logués en console et la logique continue.
- **[Prod]** `app.mail.enabled=true` + identifiants SMTP valides (Gmail App Password recommandé).

## Dépannage
- **[Erreur Base64 secret]**: Assurez-vous que `APP_JWT_SECRET` est Base64/Base64URL ou texte brut >= 32 octets.
- **[SMTP unreachable]**: Laisser `app.mail.enabled=false` en dev; vérifier DNS/proxy en prod.
- **[403 sur vues]**: Les routes `GET /dashboard/**` et `GET /offers/**` sont publiques dans `SecurityConfig`.

## Scripts utiles
- **[Build]** `mvn -q -DskipTests package`
- **[Run]** `mvn spring-boot:run`

---

Made with Spring Boot 3 ☕
