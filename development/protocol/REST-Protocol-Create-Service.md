# Create Service - REST Protocol

Source: https://apereo.github.io/cas/development/protocol/REST-Protocol-Create-Service.html

Support is enabled by including the following in your overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-rest-services"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.rest.services.attribute-name` (Required; type `String`): Authorization attribute name required by the REST endpoint in order to allow for the requested operation. Attribute must be resolvable by the authenticated principal, or must have been already.
- `cas.rest.services.attribute-value` (Required; type `String`): Matching authorization attribute value, pulled from the attribute required by the REST endpoint in order to allow for the requested operation. The attribute value may also be constructed as a regex pattern.

Invoke CAS to register applications into its own service registry. The REST call must be authenticated using basic authentication where credentials are authenticated and accepted by the existing CAS authentication strategy, and furthermore the authenticated principal must be authorized with a pre-configured role/attribute name and value that is designated in the CAS configuration via the CAS properties. The body of the request must be the service definition that shall be registered in JSON format and of course, CAS must be configured to accept the particular service type defined in the body. The accepted media type for this request is `application/json`.

```bash
POST /cas/v1/services HTTP/1.0
```

…where body of the request may be:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "...",
  "name" : "...",
  "id" : 1,
  "description": "..."
}
```

A successful response will produce a `200` status code in return.
