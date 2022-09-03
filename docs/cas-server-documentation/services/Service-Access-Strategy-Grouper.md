---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Service Access Strategy - Grouper

The grouper access strategy is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-grouper-core" %}

This access strategy attempts to locate [Grouper](https://github.com/Internet2/grouper) 
groups for the CAS principal. The groups returned by Grouper are collected as CAS attributes and examined against the list of required attributes for 
service access.

The following properties are available:

| Field        | Description                                                                  | Values                                                    |
|--------------|------------------------------------------------------------------------------|-----------------------------------------------------------|
| `groupField` | Attribute of the Grouper group when converting the group to a CAS attribute. | `NAME`, `EXTENSION`, `DISPLAY_NAME`, `DISPLAY_EXTENSION`. |

You will also need to ensure `grouper.client.properties` is available on the classpath (i.e. `src/main/resources`)
with the following configured properties:

```properties
grouperClient.webService.url = http://grouper.example.com/grouper-ws/servicesRest
grouperClient.webService.login = banderson
grouperClient.webService.password = password
```

Grouper access strategy based on group's display extension:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "test",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.grouper.services.GrouperRegisteredServiceAccessStrategy",
    "requireAllAttributes" : true,
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "grouperAttributes" : [ "java.util.HashSet", [ "faculty" ] ]
    },
    "groupField" : "DISPLAY_EXTENSION"
  }
}
```
      
While the `grouper.client.properties` is a hard requirement and must be presented, 
configuration properties can always be assigned to the strategy
to override the defaults: 

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "test",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.grouper.services.GrouperRegisteredServiceAccessStrategy",
    "configProperties" : {
      "@class" : "java.util.HashMap",
      "grouperClient.webService.url" : "http://grouper.example.com/grouper-ws/servicesRest"
    },
    "groupField" : "DISPLAY_EXTENSION"
  }
}
```

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
