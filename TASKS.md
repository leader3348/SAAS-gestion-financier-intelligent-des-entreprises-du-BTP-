# TASKS — MizanFlow
Légende : ✅ réalisé et vérifié · 🟡 réalisé, NON vérifié (non exécuté) · ⬜ non réalisé

## Fondations
- 🟡 Spring Boot 3.3 / Java 21, modules par domaine, Flyway, config par variables d'environnement
- 🟡 Sécurité : BCrypt(12), JWT access 15 min, refresh avec rotation + détection de réutilisation, RBAC `@PreAuthorize`, CORS restrictif, rate limiting auth, erreurs sans fuite d'info
- 🟡 Multi-tenancy : tenant issu de la base via l'utilisateur authentifié, `findByIdAndTenantId` partout, garde-fou JPA `@PrePersist/@PreUpdate`, FK composites `(id, tenant_id)` en SQL
- 🟡 Abonnements : plans, essai, quotas utilisateurs/projets appliqués côté backend
- 🟡 Audit logs (inscription, connexion, workflow, factures, paiements)
## Métier
- 🟡 Clients (recherche, pagination) · 🟡 Projets (réf. auto, quota) · 🟡 Prestations (DRAFT→SUBMITTED→APPROVED/REJECTED→INVOICED, historique, rejet motivé, verrouillage)
- 🟡 Factures depuis prestations approuvées (verrou pessimiste, anti double facturation), émission numérotée, annulation motivée
- 🟡 Paiements partiels/totaux avec verrou, OVERDUE calculé, dashboard sur données réelles
## Frontend
- ✅ Landing, tarifs, légal (modèles), login/inscription (Zod), shell responsive, FR + bascule AR/RTL (navigation uniquement)
- ✅ Dashboard, Clients, Projets, Travaux (soumettre/approuver/rejeter), Factures (émettre/payer) — build OK ; 🟡 non testés contre un vrai backend
## Tests
- ✅ Frontend : 3 tests Vitest passés
- 🟡 Backend : 14 tests unitaires/intégration écrits, NON exécutés (Maven/Docker indisponibles dans l'environnement de génération)
## Ajouts SaaS (v0.2)
- 🟡 Console SUPER_ADMIN : stats (entreprises, essais, MRR calculé), liste, suspension/réactivation, validation manuelle des paiements d'abonnement (migration V3) ; SUPER_ADMIN initial via BOOTSTRAP_ADMIN_* (aucun compte par défaut)
- 🟡 Abonnement côté client : plan courant, usage vs quotas, demande d'activation avec référence de virement, bannière d'essai
- 🟡 Équipe : ajout de membres (quota appliqué, mot de passe temporaire affiché une fois), désactivation avec révocation des sessions, changement de mot de passe
- ✅ Nouveau design (build OK) : landing premium (hero, maquette, tarifs mensuel/annuel, FAQ), shell avec icônes, support RTL ; non testé visuellement dans un navigateur
## Ajouts v0.3
- 🟡 Extension navigateur MV3 (`extension/`) : connexion, saisie rapide, validation/rejet, résumé financier, badge périodique, options serveur. Vérifiée : syntaxe JS (`node --check`) + manifeste JSON valide ; NON chargée dans un navigateur
- 🟡 Backend : `GET /work-logs/summary`, un FIELD_WORKER ne voit plus que ses propres prestations (limite signalée en v0.1 corrigée), CORS par motifs (`chrome-extension://*`)
## Non réalisé (ordre de priorité suggéré)
- ⬜ Devis + PDF · ⬜ Situations de travaux · ⬜ Génération PDF/Excel · ⬜ Upload de preuves (photos) + stockage S3/local
- ⬜ Dépenses/matériaux/stock · ⬜ Fournisseurs (table prête) · ⬜ Moteur d'automatisation + notifications (tables prêtes) · ⬜ Portail client
- ⬜ Invitations par email · ⬜ Renouvellement/expiration automatiques (job planifié) · ⬜ Tests backend des nouveaux modules
- ⬜ Données de démo (profil DEMO) · ⬜ Import CSV · ⬜ Recherche globale · ⬜ IA · ⬜ Tests de concurrence · ⬜ HTTPS/Nginx prod (certificats)
