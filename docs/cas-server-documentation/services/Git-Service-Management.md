---
layout: default
title: CAS - Git Service Registry
category: Services
---

{% include variables.html %}

# Git Service Registry

This registry reads services definitions from remote or local git repositories. Service definition files are expected to be
either [JSON](JSON-Service-Management.html) or [YAML](YAML-Service-Management.html) files. The contents of the repository is pulled at defined intervals and changes to service definitions are committed and 
pushed to the defined remotes.

Support is enabled by adding the following module into the overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-git-service-registry" %}

{% include casproperties.html properties="cas.service-registry.git" %}

<div class="alert alert-warning"><strong>No Interference!</strong><p>
Be careful to not manually modify the state of the git repository directory that is cloned on the local server. By doing so, you risk 
interfering with CAS' own service management processes and ultimately may end up corrupting the state of the git repository.
</p></div>

The service registry is also able to auto detect changes as it will 
pull changes from defined remotes periodically. It will monitor changes to recognize
file additions, removals and updates and will auto-refresh CAS so changes may happen instantly.
