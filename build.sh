#!/bin/bash
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
echo "1/3 Building frontend..."
(cd "$DIR/front" && npm run build)
echo "2/3 Deploying to gateway static..."
rsync -a --delete "$DIR/front/dist/" "$DIR/gateway/src/main/resources/static/"
echo "3/3 Packaging..."
(cd "$DIR" && ./mvnw package -DskipTests)
echo "Done. Run: docker compose up --build"

