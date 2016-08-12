#!/bin/bash

LOOP=10
echo $REPO >> /var/log/setUpLog

if [ -z $REPO ]; then
    exit 1
fi

rm -rf $REPO systemConfig
git clone $REPO systemConfig

node installplugins.js >> /var/log/setUpLog && \
node createcredentials.js >> /var/log/setUpLog && \
node createdocker.js >> /var/log/setUpLog && \
node jobMonitor.js >> /var/log/setUpLog && \
node createaccounts.js >> /var/log/setUpLog