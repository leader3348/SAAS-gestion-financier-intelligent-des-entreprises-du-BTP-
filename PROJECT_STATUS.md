# PROJECT_STATUS — MizanFlow (v0.3.0, fondation + couche SaaS + extension navigateur)
Date : 15/11/2026. Ce dépôt est une **fondation solide**, pas le produit complet décrit dans le cahier des charges.

| Élément | État |
|---|---|
| Frontend `npm run build` | ✅ exécuté, OK |
| Frontend `npm test` | ✅ 3/3 tests passent |
| Backend compilation Maven | ⚠️ NON exécutée (pas de Maven ni d'accès à Maven Central dans l'environnement de génération). Seule une vérification de syntaxe `javac` a été faite : aucune erreur de syntaxe, mais pas de vérification de types |
| Backend tests | ⚠️ écrits, jamais exécutés |
| Migrations Flyway / PostgreSQL | ⚠️ jamais exécutées |
| Docker Compose | ⚠️ jamais exécuté (Docker indisponible) |
Premier réflexe à avoir : lancer `cd backend && mvn verify` (Docker requis pour `ApiIntegrationTest`) et corriger les éventuelles erreurs de compilation/migration.

## Hypothèses métier documentées
- Numérotation `FAC-AAAA-NNNN` par entreprise, sérialisée par verrou de ligne sur `companies`. Un trou de numérotation n'est pas garanti impossible en cas d'annulation d'une transaction : à valider avec un comptable.
- Une prestation approuvée est facturée en totalité (quantité restante) ; la facturation partielle par quantité nécessite le module Situations (non fait).
- TVA par défaut 20 % ; taux saisi par facture.
- Plans et prix : hypothèses commerciales (149/299/599 DH).
- Jeton d'accès en mémoire, refresh token en localStorage (compromis MVP ; passer à un cookie HttpOnly + CSRF pour durcir).
