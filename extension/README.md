# Extension navigateur MizanFlow (Chrome / Edge / Brave — Manifest V3)
Saisie rapide des prestations, validation par les responsables, indicateurs financiers, badge (à valider / à corriger). Aucune dépendance, aucun build.

## Installation (développement)
1. Démarrer le backend avec CORS autorisant l'extension :
   `CORS_ORIGINS="http://localhost:5173,chrome-extension://*"` (le CORS n'est pas la frontière de sécurité : l'API n'utilise aucun cookie, seulement un jeton Bearer).
2. Chrome → `chrome://extensions` → activer « Mode développeur » → « Charger l'extension non empaquetée » → choisir ce dossier `extension/`.
3. Cliquer sur l'icône MizanFlow, se connecter avec un compte existant.
Autre serveur : clic droit sur l'icône → Options → saisir l'URL HTTPS (le navigateur demandera la permission).

## Fonctionnalités par rôle
| Rôle | Onglets |
|---|---|
| COMPANY_ADMIN / PROJECT_MANAGER | Résumé, À valider (approuver / rejeter avec motif), Saisir |
| ACCOUNTANT | Résumé |
| FIELD_WORKER | Saisir, Mes prestations (soumettre / corriger les rejets) |
Les autorisations restent appliquées par le backend ; l'extension n'affiche que ce que le rôle permet.

## Sécurité
Jeton d'accès dans `chrome.storage.session` (effacé à la fermeture du navigateur) · refresh token en `storage.local` · rotation protégée par un verrou inter-contextes · rendu DOM sans `innerHTML` · permissions minimales (`storage`, `alarms`, hôte localhost ; autres hôtes à la demande).
## Limites connues
Non testée dans un navigateur réel lors de sa génération (seulement contrôle de syntaxe) · l'onglet « À valider » lit les 100 prestations les plus récentes · pas d'envoi de photos.
