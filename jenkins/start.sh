#!/bin/bash

LOOP=10
echo $REPO >> /var/log/setUpLog

if [ -z $REPO ]; then
    exit 1
fi

rm -rf $REPO systemConfig
git clone $REPO systemConfig
while [ ! -a systemConfig/config.json ]
do
    LOOP=LOOP-1
    sleep 5s
    if [LOOP -eq 0]; then
        exit 1
    fi
done

node installplugins.js >> /var/log/setUpLog && \
node createcredentials.js >> /var/log/setUpLog && \
node createdocker.js >> /var/log/setUpLog && \
node jobMonitor.js >> /var/log/setUpLog && \
node createaccounts.js >> /var/log/setUpLog