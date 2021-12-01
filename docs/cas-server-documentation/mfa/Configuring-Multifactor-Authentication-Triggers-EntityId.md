---
layout: default
title: CAS - Multifactor Authentication Triggers
category: Multifactor Authentication
---

{% include variables.html %}

# Entity Id Request Parameter - Multifactor Authentication Triggers

In situations where authentication is delegated to CAS, most commonly 
via a Shibboleth Identity Provider, the entity id may be passed as 
a request parameter to CAS to be treated as a CAS registered service.
This allows one to activate multifactor authentication policies based on the entity id that is registered
This allows one to [activate multifactor authentication policies](#Per Application) based on the entity id that is registered
in the CAS service registry. As a side benefit, the entity id can take advantage of all other CAS features
such as access strategies and authorization rules because it's just another service definition known to CAS.

To learn more about integration options and to understand how to delegate authentication to CAS 
from a Shibboleth identity provider, please [see this guide](../integration/Shibboleth.html).

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-shibboleth" %}

The `entityId` parameter may be passed as such:

```bash
https://.../cas/login?service=http://idp.example.org&entityId=the-entity-id-passed
```
