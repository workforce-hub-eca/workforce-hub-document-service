#!/usr/bin/env bash
set -euo pipefail
# Never enable set -x

if [ -z "${GCP_PROJECT_ID:-}" ]; then
    echo "ERROR: GCP_PROJECT_ID is required"
    exit 1
fi

if [ -z "${DOCUMENT_MONGODB_SECRET_ID:-}" ]; then
    echo "ERROR: DOCUMENT_MONGODB_SECRET_ID is required"
    exit 1
fi

for var in DOCUMENT_MONGODB_HOST DOCUMENT_MONGODB_PORT DOCUMENT_MONGODB_DATABASE DOCUMENT_MONGODB_USERNAME DOCUMENT_MONGODB_AUTH_DATABASE; do
    if [ -z "${!var:-}" ]; then
        echo "ERROR: $var is required"
        exit 1
    fi
done

echo "Waiting for Config Server to be healthy..."
max_retries=30
retry_count=0
while [ $retry_count -lt $max_retries ]; do
    if curl -s -f http://localhost:8888/actuator/health | grep -q '"status":"UP"'; then
        echo "Config Server is up."
        break
    fi
    echo "Config Server not ready, retrying in 2 seconds..."
    retry_count=$((retry_count + 1))
    sleep 2
done

if [ $retry_count -eq $max_retries ]; then
    echo "ERROR: Config Server did not become healthy in time."
    exit 1
fi

echo "Retrieving MongoDB password from Secret Manager..."
TEMP_PASS=$(/snap/bin/gcloud secrets versions access latest --secret="${DOCUMENT_MONGODB_SECRET_ID}" --project="${GCP_PROJECT_ID}")

if [ -z "$TEMP_PASS" ]; then
    echo "ERROR: Retrieved MongoDB password is empty."
    exit 1
fi

export SPRING_MONGODB_HOST="$DOCUMENT_MONGODB_HOST"
export SPRING_MONGODB_PORT="$DOCUMENT_MONGODB_PORT"
export SPRING_MONGODB_DATABASE="$DOCUMENT_MONGODB_DATABASE"
export SPRING_MONGODB_USERNAME="$DOCUMENT_MONGODB_USERNAME"
export SPRING_MONGODB_AUTHENTICATIONDATABASE="$DOCUMENT_MONGODB_AUTH_DATABASE"

export DOCUMENT_MONGODB_PASSWORD="$TEMP_PASS"
export SPRING_MONGODB_PASSWORD="$TEMP_PASS"
unset TEMP_PASS

echo "Starting Document Service..."
exec /usr/bin/java -jar /opt/workforce-hub/apps/document-service-0.0.1-SNAPSHOT.jar
