#!/bin/bash
# Ornaments: the one a character shows and the ones it owns (players lives in
# aegnor_login), and the ORNEMENT admin command.
set -euo pipefail

mariadb -u root -p"${MARIADB_ROOT_PASSWORD}" aegnor_login <<'SQL'
ALTER TABLE players
  ADD COLUMN IF NOT EXISTS `ornament` INT NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS `ornaments` TEXT NOT NULL DEFAULT '';
INSERT INTO `administration.commands` (command, args, description)
SELECT 'ORNEMENT', '[idOrnement] [Pseudo]', 'Donne et affiche un ornement (0 : aucun).'
WHERE NOT EXISTS (SELECT 1 FROM `administration.commands` WHERE command = 'ORNEMENT');
SQL
