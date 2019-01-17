#!/usr/bin/env bash

REMOTE_HOME=${REMOTE_USERNAME}@${HOST}:/home/${REMOTE_USERNAME}

# LOCAL build artifact
UFAG_JAR=$HOME/build/velet5/ufag/target/scala-2.12/ufag-assembly-0.1.jar
# REMOTE path of the artifact
UFAG_JAR_DESTINATION=${REMOTE_HOME}/ufag/dist/ufag-$(date +%Y-%m-%d-%H-%M-%S).jar

# DECODING ssh private key
echo "${SSH_PRIVATE_KEY_BASE64_ENCODED}" | base64 --decode > $HOME/.ssh/deploy_rsa

# SETTING up ssh key
chmod 0600 $HOME/.ssh/deploy_rsa
eval `ssh-agent`
ssh-add $HOME/.ssh/deploy_rsa

# UPLOAD artifact to remote host
scp \
    -o StrictHostKeyChecking=no \
    -i $HOME/.ssh/deploy_rsa \
    ${UFAG_JAR} ${UFAG_JAR_DESTINATION}

# UPLOAD restart script to remote host
scp \
    -o StrictHostKeyChecking=no \
    -i $HOME/.ssh/deploy_rsa \
    $HOME/build/velet5/ufag/scripts/restart.sh ${REMOTE_HOME}/ufag/

# FIXING permissions
ssh \
    -o StrictHostKeyChecking=no \
    -i $HOME/.ssh/deploy_rsa \
    ${REMOTE_USERNAME}@${HOST} "chmod u+x /home/${REMOTE_USERNAME}/ufag/restart.sh"

# EXECUTING restarting script
ssh \
    -o StrictHostKeyChecking=no \
    -i $HOME/.ssh/deploy_rsa \
    ${REMOTE_USERNAME}@${HOST} "cd /home/${REMOTE_USERNAME}/ufag/; nohup ./restart.sh > ufag.log 2>&1"
