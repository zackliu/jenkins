#!/bin/bash

SYSCONFIGURL="https://github.com/zackliu/jenkins.git"
LOOP=10

git clone $SYSCONFIGURL systemConfig
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