---
layout: default
title: CAS - Docker Installation
---

# Docker Installation
Upon every release of the CAS software, docker images are tagged and pushed
to the Apereo CAS repository on [Docker Hub](https://hub.docker.com/r/apereo/cas/).
Images can be pulled down via the following command:

```xml
docker pull apereo/cas:v[A.B.C]
```

...where `[A.B.C]` represents the image tag that is mapped to the CAS server version.

## Overview
A dockerized CAS deployment simply is an existing CAS overlay project that is wrapped by Docker.
The overlay project already includes an embedded Jetty instance to handle the deployment of CAS. 
The overlay project also includes an embedded Maven so that builds and deployments of CAS 
would not require a separate step to download and configure Maven. 

The docker build is simply instructed to clone the CAS overlay project, use the embedded Maven
instance to package and build it and finally uses the embedded Jetty instance to deploy the final
CAS web application. Once CAS is running, it will be available under ports `8080` and `8443`
which can be mapped to the host ports `80` and `443`. 

## Requirements

- Minimum of Docker version 1.9.x

Additional instructions on how to use CAS docker images for deployment
and adoption will become available in the future. For now, [the following
guide](https://github.com/apereo/cas/tree/dockerized-caswebapp)
may serve as a template on how to get started with a local deployment
process.
