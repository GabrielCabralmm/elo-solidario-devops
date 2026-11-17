#!/bin/bash
set -euo pipefail

# Variáveis
RM="${RM:-rm563230}"
LOCATION="${LOCATION:-brazilsouth}"
RESOURCE_GROUP="rg-elosolidario-${RM}"
SQL_SERVER="sql-elosolidario-${RM}"
SQL_DATABASE="db-elosolidario"
SQL_ADMIN="${SQL_ADMIN:-elosolidarioadmin}"
APP_SERVICE_PLAN="plan-elosolidario-${RM}"
WEB_APP="elo-solidario-${RM}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Segredos (lidos do ambiente ou digitados, nunca gravados no código)
if [ -z "${SQL_ADMIN_PASSWORD:-}" ]; then
  read -r -s -p "Senha do administrador do Azure SQL (mín. 8 caracteres, com maiúscula, minúscula, número e símbolo): " SQL_ADMIN_PASSWORD
  echo
fi
if [ -z "${JWT_SECRET:-}" ]; then
  JWT_SECRET="$(openssl rand -base64 48 | tr -d '\n')"
  echo "JWT_SECRET gerado automaticamente. Guarde este valor para a variável da pipeline:"
  echo "$JWT_SECRET"
fi
if [ -z "${RABBITMQ_URL:-}" ]; then
  read -r -s -p "URL do RabbitMQ (CloudAMQP, amqps://...): " RABBITMQ_URL
  echo
fi
if [ -z "${AI_API_KEY:-}" ]; then
  read -r -s -p "Chave da API do Gemini: " AI_API_KEY
  echo
fi

DB_URL="jdbc:sqlserver://${SQL_SERVER}.database.windows.net:1433;databaseName=${SQL_DATABASE};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30"

# Provedores
az provider register --namespace Microsoft.Sql --wait
az provider register --namespace Microsoft.Web --wait

# Grupo de recursos
az group create --name "$RESOURCE_GROUP" --location "$LOCATION" --tags projeto=EloSolidario disciplina=DevOps

# Banco de dados (Azure SQL)
az sql server create \
  --name "$SQL_SERVER" \
  --resource-group "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --admin-user "$SQL_ADMIN" \
  --admin-password "$SQL_ADMIN_PASSWORD"

az sql server firewall-rule create \
  --resource-group "$RESOURCE_GROUP" \
  --server "$SQL_SERVER" \
  --name PermitirServicosAzure \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0

MEU_IP="$(curl -s https://api.ipify.org)"
az sql server firewall-rule create \
  --resource-group "$RESOURCE_GROUP" \
  --server "$SQL_SERVER" \
  --name PermitirMeuIp \
  --start-ip-address "$MEU_IP" \
  --end-ip-address "$MEU_IP"

az sql db create \
  --resource-group "$RESOURCE_GROUP" \
  --server "$SQL_SERVER" \
  --name "$SQL_DATABASE" \
  --service-objective Basic \
  --backup-storage-redundancy Local

# Criação das tabelas, gatilhos e carga inicial
sqlcmd -S "tcp:${SQL_SERVER}.database.windows.net,1433" \
  -d "$SQL_DATABASE" \
  -U "$SQL_ADMIN" \
  -P "$SQL_ADMIN_PASSWORD" \
  -I -f 65001 -b \
  -i "${SCRIPT_DIR}/script-bd.sql"

# Serviço de aplicativo (App Service Linux com Java 21)
az appservice plan create \
  --name "$APP_SERVICE_PLAN" \
  --resource-group "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --is-linux \
  --sku B1

az webapp create \
  --name "$WEB_APP" \
  --resource-group "$RESOURCE_GROUP" \
  --plan "$APP_SERVICE_PLAN" \
  --runtime "JAVA:21-java21"

az webapp config set \
  --name "$WEB_APP" \
  --resource-group "$RESOURCE_GROUP" \
  --always-on true \
  --http20-enabled true

az webapp config appsettings set \
  --name "$WEB_APP" \
  --resource-group "$RESOURCE_GROUP" \
  --settings \
    DB_URL="$DB_URL" \
    DB_USERNAME="$SQL_ADMIN" \
    DB_PASSWORD="$SQL_ADMIN_PASSWORD" \
    JWT_SECRET="$JWT_SECRET" \
    RABBITMQ_URL="$RABBITMQ_URL" \
    AI_API_KEY="$AI_API_KEY" \
    PORT=8080 \
    WEBSITES_PORT=8080 \
    TZ=America/Sao_Paulo \
  --output none

az webapp log config \
  --name "$WEB_APP" \
  --resource-group "$RESOURCE_GROUP" \
  --application-logging filesystem \
  --docker-container-logging filesystem \
  --level information

# Resumo
echo
echo "Recursos criados no grupo $RESOURCE_GROUP ($LOCATION):"
az resource list --resource-group "$RESOURCE_GROUP" --query "[].{nome:name, tipo:type}" --output table
echo
echo "API:             https://${WEB_APP}.azurewebsites.net"
echo "Swagger:         https://${WEB_APP}.azurewebsites.net/swagger-ui.html"
echo "Servidor SQL:    ${SQL_SERVER}.database.windows.net"
echo "Banco:           ${SQL_DATABASE}"
echo "DB_URL:          ${DB_URL}"
