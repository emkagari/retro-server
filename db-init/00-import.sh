#!/bin/bash
# Imports the kit's three databases, when the MariaDB volume is created.
#
# The dumps hold tables only (no CREATE DATABASE, no USE): the databases are
# created here and each dump goes to its own. dumps/ itself isn't run by the
# entrypoint, which only reads the first level of /docker-entrypoint-initdb.d.
set -euo pipefail

DUMPS=/docker-entrypoint-initdb.d/dumps
MYSQL=(mariadb -u root -p"${MARIADB_ROOT_PASSWORD}")

for base in aegnor_login aegnor_game aegnor_web; do
  echo "== $base"
  "${MYSQL[@]}" -e "CREATE DATABASE IF NOT EXISTS \`$base\` CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci;"
  if [ -f "$DUMPS/$base.sql" ]; then
    "${MYSQL[@]}" "$base" < "$DUMPS/$base.sql"
  else
    echo "   no dump: empty database"
  fi
done

# The login gives clients the game server's address: the kit's row says
# 127.0.0.1, right for a player on this machine.
"${MYSQL[@]}" -e "SELECT id, name, \`key\` FROM aegnor_login.servers;"
