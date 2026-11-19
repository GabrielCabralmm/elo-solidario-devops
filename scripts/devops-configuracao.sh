#!/bin/bash
set -euo pipefail

# Variáveis
ORGANIZACAO="${ORGANIZACAO:?Informe a URL da organização, ex.: https://dev.azure.com/minha-org}"
PROJETO="${PROJETO:-EloSolidario}"
REPOSITORIO="${REPOSITORIO:-elo-solidario-devops}"
GITHUB_URL="${GITHUB_URL:-https://github.com/GabrielCabralmm/elo-solidario-devops.git}"
REVISOR="${REVISOR:?Informe o e-mail do revisor padrão (seu RM), ex.: rm563230@fiap.com.br}"
PROFESSOR="${PROFESSOR:-}"
GRUPO_VARIAVEIS="elo-solidario-segredos"

az extension add --name azure-devops --upgrade --only-show-errors
az devops configure --defaults organization="$ORGANIZACAO" project="$PROJETO"

# Projeto privado
if ! az devops project show --project "$PROJETO" --output none 2>/dev/null; then
  az devops project create --name "$PROJETO" --visibility private --source-control git --process Agile
fi

# Repositório importado do GitHub
if ! az repos show --repository "$REPOSITORIO" --output none 2>/dev/null; then
  az repos create --name "$REPOSITORIO" --output none
  az repos import create --git-source-url "$GITHUB_URL" --repository "$REPOSITORIO"
fi
REPO_ID="$(az repos show --repository "$REPOSITORIO" --query id --output tsv)"

# Proteção da branch main
az repos policy approver-count create \
  --repository-id "$REPO_ID" --branch main \
  --minimum-approver-count 1 --creator-vote-counts true \
  --allow-downvotes false --reset-on-source-push false \
  --blocking true --enabled true --output none

az repos policy work-item-linking create \
  --repository-id "$REPO_ID" --branch main \
  --blocking true --enabled true --output none

az repos policy required-reviewer create \
  --repository-id "$REPO_ID" --branch main \
  --required-reviewer-ids "$REVISOR" \
  --message "Revisão obrigatória do responsável pelo projeto" \
  --blocking true --enabled true --output none

# Variáveis de ambiente da pipeline (grupo de variáveis com segredos)
if ! az pipelines variable-group list --group-name "$GRUPO_VARIAVEIS" --query "[0].id" --output tsv | grep -q .; then
  az pipelines variable-group create --name "$GRUPO_VARIAVEIS" --authorize true \
    --variables DB_USERNAME="${DB_USERNAME:-elosolidarioadmin}" --output none
fi
GRUPO_ID="$(az pipelines variable-group list --group-name "$GRUPO_VARIAVEIS" --query "[0].id" --output tsv)"

definir_segredo() {
  local nome="$1"
  local valor="${!nome:-}"
  if [ -z "$valor" ]; then
    read -r -s -p "Valor de $nome: " valor
    echo
  fi
  az pipelines variable-group variable create --group-id "$GRUPO_ID" --name "$nome" --value "$valor" --secret true --output none 2>/dev/null \
    || az pipelines variable-group variable update --group-id "$GRUPO_ID" --name "$nome" --value "$valor" --secret true --output none
}

for SEGREDO in DB_URL DB_PASSWORD JWT_SECRET RABBITMQ_URL AI_API_KEY; do
  definir_segredo "$SEGREDO"
done

# Pipeline YAML
if ! az pipelines show --name "$REPOSITORIO" --output none 2>/dev/null; then
  az pipelines create --name "$REPOSITORIO" \
    --repository "$REPOSITORIO" --repository-type tfsgit --branch main \
    --yml-path azure-pipeline.yml --skip-first-run true --output none
fi

# Acesso do professor
if [ -n "$PROFESSOR" ]; then
  az devops user add --email-id "$PROFESSOR" --license-type express --send-email-invite true --output none || true
  GRUPO_ADMIN="$(az devops security group list --project "$PROJETO" \
    --query "graphGroups[?displayName=='Project Administrators'].descriptor | [0]" --output tsv)"
  az devops security group membership add --group-id "$GRUPO_ADMIN" --member-id "$PROFESSOR" --output none || true
fi

echo "Projeto:      $ORGANIZACAO/$PROJETO"
echo "Repositório:  $ORGANIZACAO/$PROJETO/_git/$REPOSITORIO"
echo "Pipeline:     $ORGANIZACAO/$PROJETO/_build"
