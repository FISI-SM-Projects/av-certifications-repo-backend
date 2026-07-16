#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROJECT_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
OUTPUT_DIR="${1:-dist/export}"
EXPORT_ROOT="$PROJECT_ROOT/$OUTPUT_DIR"
ZIP_PATH="$EXPORT_ROOT/av-certifications-repo-backend-clean.zip"

mkdir -p "$EXPORT_ROOT"
rm -f "$ZIP_PATH"

cd "$PROJECT_ROOT"

if command -v zip >/dev/null 2>&1; then
  zip -r "$ZIP_PATH" . \
    -x '.git/*' 'target/*' 'dist/*' 'exports/*' \
    -x 'storage/*' '!storage/.gitkeep' \
    -x '.idea/*' '.vscode/*' '.env' '.env.*' \
    -x '*.log' '*.tmp' '*.pdf' \
    -x '*/request.json' '*/metadata.json' '*/source-summary.json' \
    -x 'Thumbs.db' '.DS_Store'
  [ -f ".env.example" ] && zip "$ZIP_PATH" ".env.example" >/dev/null
  [ -f "storage/.gitkeep" ] && zip "$ZIP_PATH" "storage/.gitkeep" >/dev/null
else
  echo "No se encontro el comando zip." >&2
  exit 1
fi

echo "ZIP limpio generado: $ZIP_PATH"
