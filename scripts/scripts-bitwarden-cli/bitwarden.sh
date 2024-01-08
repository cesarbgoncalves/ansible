#!/bin/bash
export BW_CLIENTID=$1
export BW_CLIENTSECRET=$2
export BW_PASSWORD=$3
bw config server https://bitwarden.cesarbgoncalves.com.br
bw login --apikey
bw unlock --passwordenv BW_PASSWORD > /dev/null 2>&1

lista=[]
lista=`bw list items --folderid 29752335-d158-4a48-b036-f206289ce954 | jq -r '.[].id'`