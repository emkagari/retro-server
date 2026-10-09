#!/usr/bin/env bash
# Rebuilds data/lang: the official 1.49.6 lang files, with this server's world.
#
#   CLIENT143=<1.43 client>/resources/app/retroclient tools/lang149/build.sh
#
# Downloads Ankama's 1.49.6 files (what's missing), then patch.mjs brings in
# what the 1.43 lang files say: this server in `servers`, the maps, areas and
# sub-areas the server knows in `maps`, its hints in `hints`. Patched files get
# the official number + 1, and versions_<lang>.txt / versions.swf point at them.
# Needs curl and Node 22+. patch.mjs is bundled from the Retro Map Editor's
# packages (patch.ts there).
set -euo pipefail
cd "$(dirname "$0")"
OUT=$(cd ../.. && pwd)/data/lang
CDN=${LANG149_CDN:-https://dofusretro.cdn.ankama.com/static-data/612/lang}
LANGS=${LANG149_LANGS:-"de en es fr it nl pt"}
: "${CLIENT143:?set CLIENT143 to the resources/app/retroclient folder of a 1.43 client}"
mkdir -p "$OUT/swf"

fetch() { # url dest: skips what's already there
  [ -s "$2" ] && return 0
  curl -sf --retry 3 -o "$2.part" "$1" && mv "$2.part" "$2" || { echo "  ! $1"; rm -f "$2.part"; return 1; }
}

echo "== official lang files from $CDN"
fetch "$CDN/versions.swf" "$OUT/versions.official.swf"
for l in $LANGS; do
  fetch "$CDN/versions_$l.txt" "$OUT/versions_$l.official.txt"
  # "&f=items,en,1154|ttg,en,32|…": the client loads <pack>_<its language>_<number>.swf.
  for entry in $(tr '|' '\n' < "$OUT/versions_$l.official.txt" | sed 's/^&f=//' | grep -E '^[A-Za-z]+,[a-z]*,[0-9]+$'); do
    IFS=, read -r pack _ num <<< "$entry"
    fetch "$CDN/swf/${pack}_${l}_${num}.swf" "$OUT/swf/${pack}_${l}_${num}.swf"
  done
done

echo "== this server's world, from $CLIENT143/data/lang"
empty=$(mktemp -d)
SERVERS=${LANG149_SERVERS:-2} STATE_DIR="$empty" node patch.mjs "$OUT" "$CLIENT143/data/lang" "$empty" $LANGS
rm -rf "$empty"
