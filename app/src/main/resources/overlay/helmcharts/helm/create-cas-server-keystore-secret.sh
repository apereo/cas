#!/usr/bin/env bash

kubectl create secret generic cas-server-keystore --from-file=thekeystore=/etc/cas/thekeystore