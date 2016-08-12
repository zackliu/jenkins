#!/bin/bash
LOOP=10

if [ -z "$MONITORREPO" ]; then
    exit 1
fi

rm -rf systemConfig
git clone $MONITORREPO systemConfig


node createjobs.js