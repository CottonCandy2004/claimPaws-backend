#!/bin/bash
set -e
echo "1/3 Building frontend..."
cd front && npm run build && cd ..
echo "2/3 Deploying to gateway static..."
rsync -a --delete front/dist/ gateway/src/main/resources/static/
echo "3/3 Packaging..."
./mvnw package -DskipTests
echo "Done. Run: docker compose up --build"

