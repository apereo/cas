# RESTful Service Registry

Source: https://apereo.github.io/cas/development/services/REST-Service-Management.html

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-rest-service-registry"
}
```

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.service-registry.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.service-registry.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.service-registry.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.service-registry.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.service-registry.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.service-registry.rest.order` (Optional; type `Integer`)

| Operation | Method | Body | Response |
| --- | --- | --- | --- |
| Save | `POST` | `RegisteredService` object | `RegisteredService` object |
| Delete | `DELETE` | Service numeric id appended to the endpoint url as a path variable | None |
| Load | `GET` | None | Collection of `RegisteredService` objects. |
| FindById | `GET` | Service numeric id appended to the endpoint url as a path variable | `RegisteredService` object |
| FindById | `GET` | Service url appended to the endpoint url as a path variable | `RegisteredService` object |

All operations are expected to return a `200` status code. All other response status codes will force CAS to consider the requested operation nullified.

Operations that return a JSON payload must be designed to include sufficient type information about registered services. For example, when loading and returning a collection of services with a `GET`, the expected payload should match the following structure:

```json
[
  "java.util.ArrayList",
  [
    {
      "@class": "org.apereo.cas.services.CasRegisteredService",
      "serviceId": "^https://github.com/apereo/cas",
      "name": "CAS-GitHub",
      "id": 1
    },
    {
      "@class": "org.apereo.cas.services.CasRegisteredService",
      "serviceId": "^https://apereo.github.io",
      "name": "CAS-Blog",
      "id": 2
    }
  ]
]
```

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize itself from default JSON service definitions available to CAS. See [this guide](https://apereo.github.io/cas/development/services/AutoInitialization-Service-Management.html) for more info.
