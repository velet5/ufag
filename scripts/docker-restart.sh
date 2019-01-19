#!/usr/bin/env bash

# ENTER the app directory
cd ~/ufag

cd images

UFAG_IMAGE_TAR=$(ls | tail -n1)
UFAG_TAG=$(sed -e 's/^ufag-//' -e 's/\.tar$//' <<<"${UFAG_IMAGE_TAR}")

# REMOVE old images
rm $(ls | head -n -1)
cd ..

# LOAD image
docker load -i images/${UFAG_IMAGE_TAR}

# STOP current container
docker ps  | grep 'velet5/ufag' | awk '{ print $1 }' | xargs -r docker stop

# RUN new container
docker run \
    --rm \
    --detach \
    --network=ufag-network \
    --volume $HOME/ufag/application.conf:/application.conf \
    --volume $HOME/ufag/logs:/logs \
    --publish 8080:8080 \
    velet5/ufag:${UFAG_TAG}

# REMOVE old images
docker images --filter "before=velet5/ufag:${UFAG_TAG}" | \
    grep 'velet5/ufag' | \
    awk '{ print $3 }' | \
    xargs -r docker rmi -f
