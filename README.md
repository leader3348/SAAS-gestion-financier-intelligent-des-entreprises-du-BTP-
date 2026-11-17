# MizanFlow — SaaS B2B de gestion de chantiers et facturation (Maroc)

**Conception et développement d’une plateforme SaaS intelligente dédiée à la digitalisation, au suivi opérationnel et à la gestion financière des entreprises du BTP.**

MizanFlow relie les projets, les prestations de chantier, les validations, la facturation et le suivi des paiements dans un espace multi-entreprises, avec une interface en français et en arabe (RTL).
**État réel : fondation v0.1.0 — lire [PROJECT_STATUS.md](PROJECT_STATUS.md) et [TASKS.md](TASKS.md) avant toute démo.**

## Stack
Java 21 · Spring Boot 3.3 · Spring Security + JWT · JPA/Hibernate · PostgreSQL 16 · Flyway · React 18 + TypeScript + Vite · Tailwind · TanStack Query · React Hook Form + Zod · Recharts · Docker · Nginx · GitHub Actions.

## Lancer en développement
Prérequis : JDK 21, Maven 3.9, Node 22, PostgreSQL 16 (ou Docker).
```bash
# 1. Base
docker run -d --name mf-db -e POSTGRES_DB=mizanflow -e POSTGRES_USER=mizanflow -e POSTGRES_PASSWORD=dev -p 5432:5432 postgres:16-alpine
# 2. Backend (Flyway crée le schéma au démarrage)
cd backend
export DB_PASSWORD=dev JWT_SECRET="$(openssl rand -base64 48)"
mvn spring-boot:run          # API : http://localhost:8080  · Swagger : /swagger-ui.html
# 3. Frontend
cd ../frontend && npm install && npm run dev    # http://localhost:5173 (proxy /api -> 8080)
```
Créez votre entreprise via « Essai gratuit » (aucune donnée de démo). Pour accéder à la console SaaS, définissez `BOOTSTRAP_ADMIN_EMAIL` et `BOOTSTRAP_ADMIN_PASSWORD` (≥ 12 caractères) au premier démarrage du backend : aucun compte par défaut n'existe.

### Windows PowerShell avec PostgreSQL local

Si PostgreSQL écoute sur le port `5434`, démarrez d'abord l'instance locale (à répéter après un redémarrage de Windows) :
```powershell
$pgData = "$env:LOCALAPPDATA\MizanFlow\postgresql\data"
$pgLog = "$env:LOCALAPPDATA\MizanFlow\postgresql\postgres.log"
& "C:\Program Files\PostgreSQL\16\bin\pg_ctl.exe" -D $pgData -l $pgLog -o "-p 5434 -h 127.0.0.1" start
```

Si le rôle et la base n'existent pas encore, créez-les une fois avec un compte administrateur PostgreSQL. Remplacez `dev` par le mot de passe local choisi et réutilisez-le dans `DB_PASSWORD` :
```powershell
& "C:\Program Files\PostgreSQL\16\bin\psql.exe" -h 127.0.0.1 -p 5434 -U postgres -d postgres -W `
  -c "CREATE USER mizanflow WITH PASSWORD 'dev';" `
  -c "CREATE DATABASE mizanflow OWNER mizanflow;"
```

Dans un terminal PowerShell, configurez le backend. Le mot de passe ci-dessous est réservé au développement local ; remplacez-le pour tout environnement partagé :
```powershell
$env:DB_URL = "jdbc:postgresql://127.0.0.1:5434/mizanflow"
$env:DB_USER = "mizanflow"
$env:DB_PASSWORD = "dev"
$bytes = New-Object byte[] 48
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
$env:CORS_ORIGINS = "http://localhost:5173,chrome-extension://*"
$env:REFRESH_COOKIE_SECURE = "false" # HTTP local uniquement
Set-Location .\backend
mvn spring-boot:run
```

Dans un second terminal depuis la racine du projet, démarrez le frontend :
```powershell
Set-Location .\frontend
npm run dev -- --host 127.0.0.1 --port 5173
```
Pour capturer les e-mails localement sans les envoyer, installez [Mailpit](https://mailpit.axllent.org/docs/install/) puis démarrez-le dans un autre terminal :
```powershell
& "$env:LOCALAPPDATA\Programs\Mailpit\mailpit.exe" --smtp 127.0.0.1:1025 --listen 127.0.0.1:8025 --database "$env:LOCALAPPDATA\MizanFlow\mailpit.db" --disable-version-check
```
Interface Mailpit : [http://127.0.0.1:8025](http://127.0.0.1:8025). Sans serveur SMTP, l'indicateur de santé du backend reste à `DOWN`.

Pour arrêter l'instance PostgreSQL locale :
```powershell
& "C:\Program Files\PostgreSQL\16\bin\pg_ctl.exe" -D "$env:LOCALAPPDATA\MizanFlow\postgresql\data" stop
```

## Tout-en-un avec Docker Compose
```bash
cp .env.example .env   # remplir DB_PASSWORD et JWT_SECRET
docker compose up --build   # http://localhost:8081
```
Production : placer un reverse proxy TLS (Caddy/Nginx + Let's Encrypt) devant le service `frontend`, `CORS_ORIGINS` = votre domaine HTTPS, sauvegardes via `deploy/backup.sh`.
En production, définir `REFRESH_COOKIE_SECURE=true` derrière HTTPS. Garder `false` uniquement pour le développement local en HTTP.

## Extension navigateur
Voir [extension/README.md](extension/README.md) (Chrome/Edge/Brave, chargement non empaqueté). Backend à lancer avec `CORS_ORIGINS="http://localhost:5173,chrome-extension://*"`.

## Tests
```bash
cd backend && mvn verify      # unitaires + intégration (Testcontainers => Docker requis)
cd frontend && npm test && npm run build
```

## Sécurité (résumé)
Tenant déterminé côté serveur (jamais depuis le client) · FK composites SQL · rôles vérifiés côté backend · jeton de renouvellement navigateur en cookie HttpOnly/SameSite=Strict, renouvellement rotatif et détection de réutilisation · jeton d'accès navigateur conservé en mémoire · flux JSON historique maintenu pour l'extension · BCrypt · rate limit sur /auth · quotas d'abonnement côté backend · aucune valeur secrète par défaut (l'app refuse de démarrer sans `JWT_SECRET`).
Conformité : documents modèles (CGU/confidentialité) à faire valider par un juriste ; préparation loi 09-08/CNDP à compléter ; les factures générées doivent être validées par un professionnel.

## Documentation
[Architecture, MCD/MLD, API](docs/ARCHITECTURE.md) · [Plan PFE & rapport de livraison](docs/LIVRAISON_ET_PFE.md)
