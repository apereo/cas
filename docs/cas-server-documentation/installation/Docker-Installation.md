---
layout: default
title: CAS - Docker Installation
description: "Run CAS from the official Docker images, and build your own images from a CAS WAR overlay project."
category: Installation
---
{% include variables.html %}


# Docker Installation

Upon every release of the CAS software, docker images are tagged and pushed
to the Apereo CAS repository on [Docker Hub](https://hub.docker.com/r/apereo/cas/).
Images are tagged with the CAS version they contain, such as `8.0.2`; see
[Docker Hub](https://hub.docker.com/r/apereo/cas/tags) for the available tags. Always name a tag, because the
`latest` tag is not kept up to date. Pull an image with:

```bash
docker pull apereo/cas:${tag}
```

...where `${tag}` is the CAS version you want. Then:

```bash
docker run --quiet  --rm \
  -e SERVER_SSL_ENABLED=false -e SERVER_PORT=8080 \
  -p 8080:8080 --name casserver apereo/cas:${tag}
```

CAS should be running on http://localhost:8080/cas. To go from here to your own configured server, follow the
[Quick Start](../planning/Quick-Start.html).

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
