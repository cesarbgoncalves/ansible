#!/bin/bash
export BW_CLIENTID=$1
export BW_CLIENTSECRET=$2
export BW_PASSWORD=$3
bw config server https://bitwarden.cesarbgoncalves.com.br
bw login --apikey
bw unlock --passwordenv BW_PASSWORD > /dev/null 2>&1
