# IRS 1099 IRIS Filing Platform

## Project Overview

E-commerce web application for electronically filing IRS 1099 information returns through the IRIS A2A (Application to Application) system. Built with Spring Boot 3.x (Java 21) backend and React 18 + TypeScript frontend.

## Tech Stack

- **Backend**: Spring Boot 2.7.18, Java 17, Maven, Spring Security 5.x, Spring Data JPA
- **Frontend**: React 18, Vite, TypeScript, Tailwind CSS, Zustand, React Hook Form + Zod
- **Database**: MySQL (prod), H2 (dev)
- **Payments**: Stripe
- **AI**: Spring AI with Anthropic Claude
- **Hosting**: MochaHost (Tomcat 9, JDK 17-19, Node.js v18/v20)
- **Domain**: subdomain of visibleai.com
- **Packaging**: WAR (deployed to Tomcat 9)

## Project Structure

```
irs1099/
├── backend/                     # Spring Boot application
│   ├── pom.xml                  # Maven dependencies
│   └── src/main/java/com/irs1099/
│       ├── config/              # Security, CORS, app config
│       ├── controller/          # REST controllers
│       ├── dto/                 # Request/Response DTOs
│       ├── entity/              # JPA entities
│       ├── exception/           # Exception handling
│       ├── repository/          # Spring Data repositories
│       ├── security/            # JWT auth, UserPrincipal
│       ├── service/             # Business logic
│       │   ├── impl/            # Service implementations
│       │   ├── ai/              # Spring AI features
│       │   ├── iris/            # IRS IRIS A2A integration
│       │   ├── payment/         # Stripe integration
│       │   └── notification/    # Email notifications
│       └── util/                # Encryption, helpers
├── frontend/                    # React SPA
│   └── src/
│       ├── components/          # React components
│       ├── pages/               # Route pages
│       ├── services/            # API client
│       ├── store/               # Zustand state
│       └── types/               # TypeScript types
└── docs/                        # Documentation
```

## Development

### Backend
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

The frontend dev server proxies `/api` requests to `http://localhost:8080`.

## Key Rules

1. **Security First**: All TIN/SSN/EIN data must be encrypted with AES-256-GCM via `EncryptionUtil`. Never log or expose PII in plain text.
2. **IRS Compliance**: Follow IRS Publication 4557 for safeguarding taxpayer data. Follow Publication 5718 for IRIS A2A specifications.
3. **XML Generation**: IRIS transmissions use XML (UTF-8, no BOM). Max 100MB per transmission. Use IRS schema definitions.
4. **Auth Flow**: JWT-based authentication with BCrypt password hashing (strength 12). Access tokens expire in 1 hour, refresh tokens in 7 days.
5. **API Prefix**: All backend endpoints are under `/api` context path.
6. **Database**: Use JPA entities with Lombok. Migrations in `db/migration/`. Dev profile uses H2 with `create-drop`.
7. **Notifications**: All email notifications are async (`@Async`). Use Thymeleaf templates in `templates/email/`.

## IRS IRIS A2A Key Details

- **Submission endpoint**: POST to `/IRIntakeAcceptanceA2A/1.0/irisa2a/v1/intake-acceptance`
- **Status endpoint**: POST to `/IRIntakeAcceptanceA2A/1.0/iris/transstatusorack`
- **Auth**: OAuth2 with dual JWT (Client JWT + User JWT), RSA-signed
- **UTID format**: `UUID:IRIS:TCC::A`
- **Transmission types**: O (Original), C (Correction), R (Replacement)
- **Test environment**: Use `api.alt.www4.irs.gov` prefix

## Git Workflow

- `main` branch for production
- Feature branches for development
- Commit messages: conventional commits format
