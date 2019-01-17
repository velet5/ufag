#!/usr/bin/env bash

# ENTER the app directory
cd ~/ufag

cd images

UFAG_IMAGE_TAR=$(ls | tail -n1)
UFAG_TAG=$(sed -e 's/^ufag-//' -e 's/\.tar$//' <<<"${UFAG_IMAGE_TAR}")

# REMOVE old images
rm $(ls | head -n -5)
cd ..

# LOAD image
docker load -i images/${UFAG_IMAGE_TAR}

# STOP current container
docker ps --filter 'ancestor=velet5/ufag' -q | xargs -r docker stop
docker run \
    --rm \
    -d \
    --network=ufag-network \
    -v $HOME/ufag/application.conf:/application.conf \
    -v $HOME/ufag/logs:/logs \
    -p8080:8080 \
    velet5/ufag:${UFAG_TAG}

# REMOVE old images
docker images --filter "before=velet5/ufag:${UFAG_TAG}" -q | \
    sort | \
    head -n -4 | \
    xargs -r docker rmi
