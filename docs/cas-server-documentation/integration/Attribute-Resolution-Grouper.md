---
layout: default
title: CAS - Attribute Resolution
category: Attributes
---

{% include variables.html %}

# Grouper Attribute Resolution
     
The following configuration describes how to fetch and retrieve attributes from Grouper attribute repositories.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-grouper" %}

This option reads all the groups from [Grouper](https://github.com/Internet2/grouper) for
the given CAS principal and adopts them as CAS attributes under a `grouperGroups` multi-valued attribute.

{% include_cached casproperties.html properties="cas.authn.attribute-repository.grouper" %}

You will also need to ensure `grouper.client.properties` is available on the classpath (i.e. `src/main/resources`)
with the following configured properties:

```properties
# grouperClient.webService.url = http://192.168.99.100:32768/grouper-ws/servicesRest
# grouperClient.webService.login = banderson
# grouperClient.webService.password = password
```

You may also consider externalizing the Grouper client condfiguration file 
by modifying `/src/main/resources/grouper.client.properties` to contain the following:

```properties
grouperClient.config.hierarchy = classpath:/grouper.client.base.properties,file:/etc/cas/config/grouper.client.properties
```
    
...where `/etc/cas/config/grouper.client.properties` would then contain the actual Grouper related settings. 

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="edu.internet2.middleware" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
...
```
