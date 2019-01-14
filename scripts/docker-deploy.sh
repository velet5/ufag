#!/usr/bin/env bash

# TODO: double-check all this

UFAG_IMAGE_TAR=ufag-latest.tar

# ENTER the app directory
cd ~/ufag

# CREATE needed directories
mkdir -p images

# BACKUP image to be able to restore it later
cp ${UFAG_IMAGE_TAR} images/ufag-$(date +%Y-%m-%d-%H-%M-%S).tar

# REMOVE old images
cd images
rm $(ls | head -n -5)
cd ..

# LOAD image
sudo docker load -i ${UFAG_IMAGE_TAR}

sudo docker stop $(sudo docker ps -q  --filter ancestor=velet5/ufag:latest)
sudo docker run -d -v ~/application.conf:application.conf -v ~/logs:/logs -p8080:8080 velet5/ufag-latest