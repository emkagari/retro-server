# retro-server

[Aegnor](https://github.com/jguyet/server-1.43.7), jguyet's Dofus Retro 1.43.7
emulator, for the official Retro client 1.49.6 — the one
[retro-client](https://github.com/emkagari/retro-client) builds on. Started from jguyet's sources
and database as published; each addition is a commit of its own:

| | |
|---|---|
| Docker stack | database, login and game servers in containers; the kit's database downloaded and imported on first start |
| 1.49.6 client | the login accepts it (and only it) and sends the server list in its format; its lang files carry this server's world (`data/lang`), with the maps it lacks (`data/maps`) |
| map fixes | 554 of the kit's maps whose key or data didn't decode into a consistent map, corrected (`db-init/03-map-fixes.sql`) |
| 1.49.6 maps | the 74 maps the 1.49.6 client has and the kit's database lacks (`db-init/04-maps-149.sql`) |
| `RELOADCELLS` | reloads an edited map's cells from the database without a restart |
| fixes | watching a fight from the map's fight list; spell areas no longer wrap past a map edge |
| ornaments | Dofus 2-style nameplate frames, for retro-client's ornaments |

## Start

Needs [Docker](https://docs.docker.com/get-docker/) (Docker Desktop on Windows).

```bash
cp .env.example .env      # set DB_PASSWORD (letters and digits)
docker compose up -d
```

The first start downloads the kit's database (54 MB, into `db-init/dumps`)
and imports it: a few minutes. Later starts are immediate. `docker compose
logs -f game` shows the game server; it's up at `Server is ready`.

Ports: login 1135, game 5555, lang files 4545, database 3306 (this machine
only) — `.env` changes them.

## The client

The official Retro client 1.49.6, with this server's `config.xml`:
`client/config.xml` replaces `resources/app/retroclient/config.xml`. With
retro-client, put it in your local overlay instead (`"overlay"` in its
`retro.local.json`, at `resources/app/retroclient/config.xml`): `retro play`
copies it.

Accounts, from the kit: game `admin1` / `admin` (an administrator), web
`aegnor@admin.fr` / `admin123`.

**From another machine**: the login gives clients the game server's address,
`server.host` in `docker/game.config.properties` (127.0.0.1: this machine
only). Put this machine's address there, and in the client's `config.xml`
(`connserver url`, and `localhost` in the `dataserver` url).

## Admin commands

Typed in the game's console by an administrator (commands must be declared in
`aegnor_login`.`administration.commands`; `db-init/` declares these):

- `RELOADCELLS [id …]` — reload maps from the database (cells, size, key,
  fight places, scripted cells); without an id, the current map.
- `ORNEMENT <id> [name]` — give and show an ornament; `0` takes it off.
  Administrators can show any ornament from the client's titles window.

## Database

`db-init/` runs once, when the database volume is created: the import, then
the additions (`01-…`, `02-…`). To start again from the kit's database:

```bash
docker compose down -v    # deletes the database volume
docker compose up -d
```

## Layout

| | |
|---|---|
| `game/`, `login/` | the servers' sources (Java, compiled in their images) |
| `docker/` | images, config templates (`__DB_PASSWORD__` comes from `.env`), the lang server's nginx config |
| `db-init/` | database import and additions |
| `data/` | what the lang server serves the client at `/dofus149/` |
| `client/config.xml` | the client's config for this server |
| `tools/lang149/` | rebuilds `data/lang` from Ankama's files and a 1.43 client's (`build.sh`) |
