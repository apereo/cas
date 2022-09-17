---
layout: default
title: CAS - Configuring Service Proxy Policy
category: Services
---

{% include variables.html %}

# Configure Proxy Authentication Policy

Each registered application in the registry may be assigned a proxy policy to determine whether the service is allowed for proxy authentication. This means that
a PGT will not be issued to a service unless the proxy policy is configured to allow it. Additionally, the policy could also define which endpoint urls are in
fact allowed to receive the PGT.

Note that by default, the proxy authentication is disallowed for all applications.

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>
This feature specifically applies to applications that understand and use the CAS protocol. <br/>
Think <strong>VERY CAREFULLY</strong> before allowing an 
application to exercise proxy authentication. Blindly authorizing an application to receive a proxy-granting 
ticket may produce an opportunity for security leaks and attacks. Make sure you actually need to enable those 
features and that you understand the why. Avoid where and when you can.</p></div>

## Refuse

Disallows proxy authentication for a service. This is default policy and need not be configured explicitly.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "proxyPolicy" : {
    "@class" : "org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy"
  }
}
```

## Regex

A proxy policy that only allows proxying to PGT urls that match the specified regex pattern.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "proxyPolicy" : {
    "@class" : "org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy",
    "pattern" : "^https?://.*",
    "useServiceId": false,
    "exactMatch": false
  }
}
```

As noted earlier, the `pattern` must be specified as a valid regular expression. Furthermore, 

- If the pattern used here is identical to the pattern used by the registered service itself as specified by the `serviceId`, you may be able to reuse the same 
existing regular expression here via the `useServiceId` setting. 
- The setting `exactMatch` treats the regular expression pattern as an exact liteal and turns off the evaluation of the pattern as a regular expression in 
  favor of a literal comparison.


## REST

A proxy policy that reaches out to an external REST endpoint to determine proxy authorization.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "proxyPolicy" : {
    "@class":"org.apereo.cas.services.RestfulRegisteredServiceProxyPolicy",
    "endpoint":"http://localhost:9222",
    "headers": {
      "@class":"java.util.LinkedHashMap",
      "header": "value"
    }
  }
}
```

Endpoints must be designed to accept/process `application/json`, where the request body will contain
the contents of the registered service definition, and the requesing PGT url is passed as `pgtUrl` request parameter.
A successful `200` status code will allow proxy authentication to proceed. 
