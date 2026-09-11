#!/usr/bin/env bash
set -euo pipefail

GROUP="rg-vetsync"
LOCATION="chilecentral"
KEYVAULT_NAME="kv-vetsync-rm563197"
SQL_SERVER="sql-server-vetsync-chilecentral"
SQL_ADMIN_USER="vetsync-adm"
DATABASE="db-vetsync"
APP_NAME="app-vetsync-rm563197"

SQL_ADMIN_PASSWORD=$(openssl rand -base64 32)
MY_IP=$(curl -s https://api.ipify.org)
CONNECTION_STRING="Server=tcp:$SQL_SERVER.database.windows.net,1433;Initial Catalog=$DATABASE;User ID=$SQL_ADMIN_USER;Password=$SQL_ADMIN_PASSWORD;Encrypt=True;TrustServerCertificate=False;Connection Timeout=30;"

az provider register --namespace Microsoft.Sql --wait

az keyvault secret set \
    --vault-name "$KEYVAULT_NAME" \
    --name "sql-admin-password" \
    --value "$SQL_ADMIN_PASSWORD"

az sql server create \
    --resource-group "$GROUP" \
    --name "$SQL_SERVER" \
    --location "$LOCATION" \
    --admin-user "$SQL_ADMIN_USER" \
    --admin-password "$SQL_ADMIN_PASSWORD" \
    --enable-public-network

az sql db create \
    --resource-group "$GROUP" \
    --server "$SQL_SERVER" \
    --name "$DATABASE" \
    --service-objective Basic \
    --backup-storage-redundancy Local \
    --zone-redundant false

az sql server firewall-rule create \
    --resource-group "$GROUP" \
    --server "$SQL_SERVER" \
    --name "allow-local-development" \
    --start-ip-address "$MY_IP" \
    --end-ip-address "$MY_IP"

OUTBOUND_IPS=$(az webapp show \
    --resource-group "$GROUP" \
    --name "$APP_NAME" \
    --query possibleOutboundIpAddresses \
    --output tsv)

for IP in ${OUTBOUND_IPS//,/ }; do
    az sql server firewall-rule create \
        --resource-group "$GROUP" \
        --server "$SQL_SERVER" \
        --name "allow-app-$IP" \
        --start-ip-address "$IP" \
        --end-ip-address "$IP"
done

az webapp config connection-string set \
    --resource-group "$GROUP" \
    --name "$APP_NAME" \
    --connection-string-type SQLAzure \
    --settings DefaultConnection="$CONNECTION_STRING"
