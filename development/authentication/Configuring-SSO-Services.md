# SSO Session - Services & Applications

Source: https://apereo.github.io/cas/development/authentication/Configuring-SSO-Services.html

The single sign-on participation strategy can also be customized on a per-application basis. For additional details, please [review this guide](https://apereo.github.io/cas/development/services/Configuring-Service-SSO-Policy.html). Furthermore, a number of other features and capabilities that affect application behavior and treatment when it comes to SSO session management are listed below.

**Default Service**

In the event that no `service` is submitted to CAS, you may specify a default service url to which CAS will redirect. Note that this default service, much like all other services, MUST be authorized and registered with CAS.

The following settings and properties are available from the CAS configuration catalog:

- `cas.view.default-redirect-url` (Optional; type `String`): The default redirect URL if none is specified after a successful login or logout event. For logout redirects, this setting is closely related to and requires `LogoutProperties#isFollowServiceRedirects()`. This URL must be registered i the CAS server's service registry.

**Required Service**

CAS may be configured to require the user to authenticate from an application before access can be granted to all other registered services. Once CAS finds a record for the required application as part of the single sign-on session records, it will permit authentication attempts by all other services until the single sign-on session is destroyed.

Such validation checks can be turned off and skipped on a per-application basis:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "^https://www.example.com",
  "name": "Example",
  "id": 1,
  "description": "Example",
  "properties" : {
    "@class" : "java.util.HashMap",
    "skipRequiredServiceCheck" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "true" ] ]
    }
  }
}
```

**Participation Policies**

Additional policies can be assigned to each service definition to control participation of an application in an existing single sign-on session.

[See this guide](https://apereo.github.io/cas/development/services/Configuring-Service-SSO-Policy.html) for more info.
