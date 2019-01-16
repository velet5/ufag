#!/usr/bin/env bash

# ENTER the app directory
cd ~/ufag


# CREATE needed directories
mkdir -p images

cd images

UFAG_IMAGE_TAR=$(ls | tail -n1)
UFAG_TAG=$(sed -e 's/^ufage//' -e 's/\.tar$//' <<<"${UFAG_IMAGE_TAR}")
# REMOVE old images
rm $(ls | head -n -5)
cd ..

UFAG_CONTAINER_ID=$(cat ufag.container-id)

# LOAD image
sudo docker load -i images/${UFAG_IMAGE_TAR}

sudo docker stop ${UFAG_CONTAINER_ID}
sudo docker run -d --network=ufag-network -v $HOME/ufag/application.conf:application.conf -v $HOME/ufag/logs:/logs -p8080:8080 velet5/ufag:${UFAG_TAG} > ufag.container-id
