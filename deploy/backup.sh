#!/usr/bin/env bash
# Sauvegarde quotidienne : crontab -> 0 2 * * * /opt/mizanflow/deploy/backup.sh
set -euo pipefail
mkdir -p backups; f="backups/mizanflow-$(date +%F-%H%M).sql.gz"
docker compose exec -T db pg_dump -U mizanflow mizanflow | gzip > "$f"
find backups -name '*.sql.gz' -mtime +14 -delete; echo "OK $f"
# Restauration : gunzip -c backups/<fichier>.sql.gz | docker compose exec -T db psql -U mizanflow mizanflow
