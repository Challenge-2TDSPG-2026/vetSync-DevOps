#!/usr/bin/env bash
set -euo pipefail

GROUP="rg-vetsync"
LOCATION="chilecentral"
SKU_DEV="B1"
PLAN="plan-vetsync"
APP_NAME="app-vetsync-rm563197"
KEYVAULT_NAME="kv-vetsync-rm563197"

az provider register --namespace Microsoft.Resources --wait
az provider register --namespace Microsoft.Web --wait
az provider register --namespace Microsoft.KeyVault --wait

az group create \
    --location "$LOCATION" \
    --name "$GROUP"

az keyvault create \
    --resource-group "$GROUP" \
    --name "$KEYVAULT_NAME" \
    --location "$LOCATION" \
    --enable-rbac-authorization true

CURRENT_USER_OBJECT_ID=$(az ad signed-in-user show --query id --output tsv)
KEYVAULT_ID=$(az keyvault show \
    --resource-group "$GROUP" \
    --name "$KEYVAULT_NAME" \
    --query id \
    --output tsv)

az role assignment create \
    --assignee-object-id "$CURRENT_USER_OBJECT_ID" \
    --assignee-principal-type User \
    --role "Key Vault Secrets Officer" \
    --scope "$KEYVAULT_ID"

az appservice plan create \
    --resource-group "$GROUP" \
    --name "$PLAN" \
    --sku "$SKU_DEV" \
    --is-linux

az webapp create \
    --resource-group "$GROUP" \
    --plan "$PLAN" \
    --name "$APP_NAME" \
    --runtime "JAVA:17-java17"
