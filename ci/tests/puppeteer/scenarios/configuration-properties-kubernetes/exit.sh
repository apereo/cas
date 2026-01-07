#!/bin/bash

echo "Stopping minikube..."
minikube stop
minikube delete 2>/dev/null
