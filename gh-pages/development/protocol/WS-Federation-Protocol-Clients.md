---
layout: default
title: CAS - CAS WS Federation Protocol
category: Protocols
---

{% include variables.html %}

# Clients - WS Federation Protocol

Clients and relying parties can be registered with CAS as such:

```json
{
  "@class" : "org.apereo.cas.ws.idp.services.WSFederationRegisteredService",
  "serviceId" : "https://wsfed.example.org/.+",
  "name" : "Sample WsFed Application",
  "id" : 100
}
```

| Field                         | Description
|-------------------------------|---------------------------------------------------------------
| `serviceId`                   | Callback/Consumer url where tokens may be `POST`ed, typically matching the `wreply` parameter.
| `realm`                       | The realm identifier of the application, identified via the `wtrealm` parameter. This needs to match the realm defined for the identity provider. By default it's set to the realm defined for the CAS identity provider.
| `appliesTo`                   | Controls to whom security tokens apply. Defaults to the `realm`.

Service definitions may be managed by the [service management](../services/Service-Management.html) facility.
