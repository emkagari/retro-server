#!/bin/bash
# Declares the RELOADCELLS admin command: CommandAdmin refuses any command
# missing from administration.commands ("Commande invalide !"). Groups whose
# commands are "all" (Fondateur, Administrateur) get it with nothing else to do.
set -euo pipefail

mariadb -u root -p"${MARIADB_ROOT_PASSWORD}" aegnor_login <<'SQL'
INSERT INTO `administration.commands` (command, args, description)
SELECT 'RELOADCELLS', '[id ...]',
       'Recharge la geometrie des maps depuis la base : cellules, dimensions, cles, places, cellules scriptees. Sans argument, la map courante.'
WHERE NOT EXISTS (SELECT 1 FROM `administration.commands` WHERE command = 'RELOADCELLS');
SQL
