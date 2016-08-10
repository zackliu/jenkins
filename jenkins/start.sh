#!/bin/bash

LOOP=10
if [ -z "$REPO" ]; then
    exit 1
fi

git clone $REPO systemConfig
while [ ! -a systemConfig/config.json ]
do
LOOP=LOOP-1
if [LOOP -eq 0]; then
exit 1
fi
done

node installplugins.js >> /var/log/setUpLog && \
node createcredentials.js >> /var/log/setUpLog && \
node createdocker.js >> /var/log/setUpLog && \
node jobMonitor.js >> /var/log/setUpLog && \
node createaccounts.js >> /var/log/setUpLog