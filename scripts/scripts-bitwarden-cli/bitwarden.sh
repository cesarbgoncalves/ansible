#!/bin/bash
export BW_CLIENTID=$1
export BW_CLIENTSECRET=$2
export BW_PASSWORD=$3
export NOVA_SENHA=$4

bw config server https://bitwarden.cesarbgoncalves.com.br > /dev/null 2>&1
bw login --apikey > /dev/null 2>&1
bw unlock --passwordenv BW_PASSWORD > /dev/null 2>&1
BW_SESSION=$(bw unlock --passwordenv BW_PASSWORD | grep export | awk -F\" '{print $2}')

# lista=$(bw list items --session ${BW_SESSION} --folderid 29752335-d158-4a48-b036-f206289ce954 | jq -r '.[].id')
lista=$(bw list items --session ${BW_SESSION} --folderid 29752335-d158-4a48-b036-f206289ce954 | jq -r '.[].id')



for item in ${lista}; do
    printf "$item"
done

# bw sync