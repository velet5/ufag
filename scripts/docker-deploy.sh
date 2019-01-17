#!/usr/bin/env bash

REMOTE_HOME=${REMOTE_USERNAME}@${HOST}:/home/${REMOTE_USERNAME}

# DECODING ssh private key
echo "${SSH_PRIVATE_KEY_BASE64_ENCODED}" | base64 --decode > $HOME/.ssh/deploy_rsa

# SETTING up ssh key
chmod 0600 $HOME/.ssh/deploy_rsa
eval `ssh-agent`
ssh-add $HOME/.ssh/deploy_rsa

IMAGE_TAG=$(docker images | grep 'velet5/ufag' | grep -P '\d{8}-\d{6}' | awk '{ print $2 }')
IMAGE_FILE=ufag-${IMAGE_TAG}.tar

docker save -o ${IMAGE_FILE} velet5/ufag:${IMAGE_TAG}

# UPLOAD artifact to remote host
scp \
    -o StrictHostKeyChecking=no \
    -i $HOME/.ssh/deploy_rsa \
    ${IMAGE_FILE} ${REMOTE_HOME}/ufag/images/

# UPLOAD restart script to remote host
scp \
    -o StrictHostKeyChecking=no \
    -i $HOME/.ssh/deploy_rsa \
    $HOME/build/velet5/ufag/scripts/docker-restart.sh ${REMOTE_HOME}/ufag/

# FIXING permissions
ssh \
    -o StrictHostKeyChecking=no \
    -i $HOME/.ssh/deploy_rsa \
    ${REMOTE_USERNAME}@${HOST} "chmod u+x /home/${REMOTE_USERNAME}/ufag/docker-restart.sh"

# EXECUTING restarting script
ssh \
    -o StrictHostKeyChecking=no \
    -i $HOME/.ssh/deploy_rsa \
    ${REMOTE_USERNAME}@${HOST} "cd /home/${REMOTE_USERNAME}/ufag/; nohup ./docker-restart.sh > ufag.log 2>&1"
