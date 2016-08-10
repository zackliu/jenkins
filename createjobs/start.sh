#!/bin/bash
if [ -z "$MONITORREPO" ]; then
    exit 1
fi

git clone $MONITORREPO systemConfig

while [ ! -a systemConfig/config.json ]
do
    LOOP=LOOP-1
    if [LOOP -eq 0]; then
        exit 1
    fi
done

node createjobs.js