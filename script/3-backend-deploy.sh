#!/usr/bin/env bash
set -euo pipefail

GROUP="rg-vetsync"
APP_NAME="app-vetsync-rm563197"
KEYVAULT_NAME="kv-vetsync-rm563197"
SQL_SERVER="sql-server-vetsync-chilecentral"
DATABASE="db-vetsync"
SQL_ADMIN_USER="vetsync-adm"

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd -- "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_DIR"

if [[ ! -x ./mvnw ]]; then
    chmod +x ./mvnw
fi

echo "Running backend tests..."
./mvnw clean verify

JAR_PATH=$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' -print -quit)
if [[ -z "$JAR_PATH" ]]; then
    echo "No executable JAR was found in $PROJECT_DIR/target" >&2
    exit 1
fi

echo "Assigning a managed identity to the Web App..."
PRINCIPAL_ID=$(az webapp identity assign \
    --resource-group "$GROUP" \
    --name "$APP_NAME" \
    --query principalId \
    --output tsv)

KEYVAULT_ID=$(az keyvault show \
    --resource-group "$GROUP" \
    --name "$KEYVAULT_NAME" \
    --query id \
    --output tsv)

if ! az role assignment list \
    --assignee-object-id "$PRINCIPAL_ID" \
    --scope "$KEYVAULT_ID" \
    --role "Key Vault Secrets User" \
    --query "[].id" \
    --output tsv | grep -q .; then
    az role assignment create \
        --assignee-object-id "$PRINCIPAL_ID" \
        --assignee-principal-type ServicePrincipal \
        --role "Key Vault Secrets User" \
        --scope "$KEYVAULT_ID" >/dev/null
fi

SECRET_URI=$(az keyvault secret show \
    --vault-name "$KEYVAULT_NAME" \
    --name "sql-admin-password" \
    --query id \
    --output tsv)

SPRING_DATASOURCE_URL="jdbc:sqlserver://$SQL_SERVER.database.windows.net:1433;databaseName=$DATABASE;encrypt=true;trustServerCertificate=false;loginTimeout=30"

az webapp config appsettings set \
    --resource-group "$GROUP" \
    --name "$APP_NAME" \
    --settings \
    "SPRING_DATASOURCE_URL=$SPRING_DATASOURCE_URL" \
    "SPRING_DATASOURCE_USERNAME=$SQL_ADMIN_USER" \
    "DB_PASSWORD=@Microsoft.KeyVault(SecretUri=$SECRET_URI)" \
    "SPRING_FLYWAY_ENABLED=true"

echo "Deploying $JAR_PATH..."
az webapp deploy \
    --resource-group "$GROUP" \
    --name "$APP_NAME" \
    --src-path "$JAR_PATH" \
    --type jar

echo "Backend deployed at: https://$APP_NAME.azurewebsites.net"
