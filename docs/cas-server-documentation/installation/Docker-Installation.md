---
layout: default
title: CAS - Docker Installation
category: Installation
---
{% include variables.html %}


# Docker Installation

Upon every release of the CAS software, docker images are tagged and pushed
to the Apereo CAS repository on [Docker Hub](https://hub.docker.com/r/apereo/cas/).
Images can be pulled down via the following command:

```bash
docker pull apereo/cas
```

...where `[A.B.C]` represents the image tag that is mapped to the CAS server version.
        
Then:

```bash
docker run --rm \
  -e SERVER_SSL_ENABLED=false -e SERVER_PORT=8080 \
  -p 8080:8080 --name casserver apereo/cas
```

CAS should be running on http://localhost:8080/cas.

## Overview

A dockerized CAS deployment is an existing [CAS overlay project](WAR-Overlay-Installation.html) that is wrapped by Docker.
The overlay project already includes an embedded container to handle the deployment of CAS.
The overlay project also includes an embedded build tool so that builds and deployments of CAS 
would not require a separate step to download and configure choices. 

The docker images that are hosted on Docker Hub are *mostly* meant to be used
as quickstarts and demos. You may also be able to use them as
base images to add your customizations into the image. The image
is built out of an existing [CAS WAR Overlay](WAR-Overlay-Installation.html).

When you generate a CAS WAR Overlay project using the [CAS Initializr](WAR-Overlay-Initializr.html)., 
please refer to the instructions provided in the `README.md`
file to review options for building CAS Docker images.
  
## Kubernetes & Helm

To learn how to use a CAS Helm chart to deploy CAS on a Kubernetes cluster, please [see this](Kubernetes-Helm-Deployment.html). 
