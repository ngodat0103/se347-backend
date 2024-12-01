#!/bin/bash
mkdir -p /tmp/secrets
ssh-keygen -t rsa -b 2048 -f /tmp/secrets/id_rsa -N ""
chmod 400 /tmp/secrets/id_rsa
chmod 400 /tmp/secrets/id_rsa.pub