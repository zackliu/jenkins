#!/bin/bash

LOOP=10
echo $REPO >> /var/log/setUpLog

if [ -z $REPO ]; then
    exit 1
fi

rm -rf $REPO systemConfig
git clone $REPO systemConfig

#mount Azure File Storage
if [ -z $MOUNTURL ]; then
    MOUNTURL='ciservicestoragepool.file.core.windows.net/jobsconfig'
fi

if [ -z $MOUNTACCOUNT ]; then
    MOUNTACCOUNT='ciservicestoragepool'
fi

if [ -z $MOUNTPASSWORD ]; then
    MOUNTPASSWORD='INNFe+3A15rLJ+fSv89U7MLV4bJJNCSDZhZt2nEwuqwJPp5NrvSJapM8C9O2eeuN/ZUxlMXGo8NbdhObfGAwDw=='
fi

mkdir /mnt/mount

mount -t cifs //$MOUNTURL /mnt/mount -o vers=3.0,user=$MOUNTACCOUNT,password=$MOUNTPASSWORD,dir_mode=0777,file_mode=0777 || exit -1


if [ -d /mnt/mount/jobs ]; then
    cp -r /mnt/mount/jobs /var/jenkins_home
else
    mkdir /mnt/mount/jobs
fi

if [ ! -d /var/jenkins_home/jobs ]; then
    mkdir /var/jenkins_home/jobs
fi

chown -R jenkins jobs && chgrp -R jenkins jobs
chown jenkins /mnt/mount/jobs && chgrp jenkins /mnt/mount/jobs


lsyncd -rsync /var/jenkins_home/jobs /mnt/mount/jobs >> /var/log/setUpLog

#node installplugins.js >> /var/log/setUpLog && \
node createcredentials.js >> /var/log/setUpLog && \
node createdocker.js >> /var/log/setUpLog && \
node jobMonitor.js >> /var/log/setUpLog && \
node createaccounts.js >> /var/log/setUpLog