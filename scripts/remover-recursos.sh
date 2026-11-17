#!/bin/bash
set -euo pipefail

RM="${RM:-rm563230}"
RESOURCE_GROUP="rg-elosolidario-${RM}"

az group delete --name "$RESOURCE_GROUP" --yes --no-wait
echo "Remoção do grupo $RESOURCE_GROUP solicitada."
