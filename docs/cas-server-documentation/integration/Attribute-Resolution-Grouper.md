---
layout: default
title: CAS - Attribute Resolution
category: Attributes
---

{% include variables.html %}

# Grouper Attribute Resolution
     
The following configuration describes how to fetch and retrieve attributes from Grouper attribute repositories.

This option reads all the groups from [Grouper](https://incommon.org/software/grouper/) for
the given CAS principal and adopts them as CAS attributes under a `grouperGroups` multi-valued attribute.

{% include casproperties.html properties="cas.authn.attribute-repository.grouper" %}

You will also need to ensure `grouper.client.properties` is available
on the classpath (i.e. `src/main/resources`)
with the following configured properties:

```properties
# grouperClient.webService.url = http://192.168.99.100:32768/grouper-ws/servicesRest
# grouperClient.webService.login = banderson
# grouperClient.webService.password = password
```

