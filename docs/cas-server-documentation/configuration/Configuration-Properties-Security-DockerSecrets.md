---
layout: default
title: CAS - Securing Configuration Properties
category: Configuration
---

{% include variables.html %}

# Configuration Security - Docker Secrets

Docker secrets provide a mechanism to securely store data that can be read by applications at 
runtime. Secrets play a critical role in storing sensitive data separately from CAS. 
This includes data such as passwords, hostnames, encryption keys, and more.

Note that currently, Docker secrets are only available to swarm services. This 
means CAS standalone containers cannot access secrets. Therefore, to use these secrets, we must configure 
your cluster for swarm using the command:

```bash
docker swarm init --advertise-addr <MANAGER-IP>
```

...where `<MANAGER-IP>` is the IP address assigned by Docker to the manager node. On Docker Desktop for 
Windows and Mac, we can simplify the command:
                                              
```bash
docker swarm init
```
     
The CAS integration with Docker secrets works by scanning configuration properties 
that are found by default at `/run/secrets`. The properties are then loaded into CAS
and made available to the application context and its environment. This default configuration directory
can be configured via the system property (or environment variable) `CAS_DOCKER_SECRETS_DIRECTORY`.
               
Note that the filenames of the secrets must match the property names that CAS expects. For example,
your secret might be named `/run/secrets/cas.some.fancy.setting` with a sensitive value. At runtime, CAS
will attempt to locate and read the secret from the file and apply it to the setting `cas.some.fancy.setting`.
                
To activate this configuration mode, you will need to make sure `CONTAINER=true` is set either as an environment
variable or a system property. This setting is used to determine whether CAS is running inside a container
and will activate the Docker-relevant configuration source.
