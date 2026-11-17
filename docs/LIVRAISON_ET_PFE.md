# Rapport de livraison (honnête) et matière PFE

## Réalisé et vérifié par exécution
- Frontend : `npm run build` OK ; 3 tests Vitest OK (formatage MAD, libellés, validation du formulaire d'inscription).
- Contrôle de syntaxe Java (javac, sans dépendances) : aucune erreur de syntaxe.
## Écrit mais NON vérifié (aucune exécution possible dans l'environnement de génération : pas de Maven/Maven Central, Docker, PostgreSQL)
- Backend complet (voir TASKS.md), migrations Flyway, Dockerfiles/Compose, CI GitHub Actions, 14 tests backend.
- **Aucun résultat de test backend n'est revendiqué.** Prévoir une première session de stabilisation (`mvn verify`).
## Non réalisé
Voir la section « Non réalisé » de TASKS.md (devis, situations, PDF/Excel, preuves photo, dépenses/stock, automatisation/notifications, portail client, admin SaaS, démo, import CSV, IA…). Les critères d'acceptation du cahier des charges ne sont donc **pas** tous atteints.
## Intégrations externes (aucune configurée)
SMTP (variables MAIL_*), WhatsApp Business API, passerelle de paiement, S3 : à créer avec interfaces + configuration réelle ; aucun envoi/paiement simulé.
## Avant commercialisation
Stabiliser et tester le backend ; terminer devis/situations/PDF ; admin SaaS + validation manuelle des abonnements ; validation comptable/fiscale des factures ; CGU/confidentialité par un juriste + démarches CNDP ; HTTPS ; tests de charge/concurrence ; pilote avec 2–3 entreprises réelles et validation des prix.

## Plan du rapport PFE (à rédiger avec vos vrais éléments)
1. Contexte et problématique (fragmentation Excel/WhatsApp/PDF dans le BTP). 2. Analyse des besoins (rôles, cas d'usage — à compléter par de vraies interviews). 3. Étude de l'existant (Batigest, Sage, Odoo, Excel… à analyser vous-même). 4. Conception : architecture modulaire, multi-tenancy, machine à états, MCD/MLD (docs/ARCHITECTURE.md). 5. Réalisation : sécurité JWT/rotation, intégrité financière (verrous, BigDecimal), UI. 6. Tests : n'indiquer que les résultats réellement obtenus. 7. Perspectives : modules non réalisés, IA (prévision de retards, OCR), paiement en ligne.
Ne pas inventer d'enquêtes ni de résultats.
