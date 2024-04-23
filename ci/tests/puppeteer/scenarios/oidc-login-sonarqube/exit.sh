#!/bin/bash

echo "Removing sonarqube from k3d so we don't monopolize the ingress host mapping"
helm delete -n sonarqube sonarqube
