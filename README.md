BookHub – Backend
Backend de l’application BookHub, une bibliothèque communautaire.
API REST sécurisée permettant la gestion des utilisateurs, rôles, livres, réservations et emprunts.


Stack technique
Java 21
Spring Boot 3
Spring Web
Spring Data JPA
Spring Security
JWT (JSON Web Token)
Lombok
Validation Bean
Gradle
SQL Server (prod)
H2 (tests)


Architecture
Architecture en couches :
Controller
Service
Repository
Entity
DTO
Security

Configuration
Les variables sensibles sont définies dans un fichier .env

Lancer le projet

Démarrage en développement :
./gradlew bootRun


API disponible sur :
http://localhost:8080


Build :
./gradlew clean build

Tests :

./gradlew test


