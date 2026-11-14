# REST Attribute Resolution

Source: https://apereo.github.io/cas/development/integration/Attribute-Resolution-REST.html

The following configuration describes how to fetch and retrieve attributes from REST attribute repositories.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.attribute-repository.rest[0].url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.attribute-repository.rest` (Optional; type `List<RestPrincipalAttributesProperties>`): Retrieve attributes from multiple REST endpoints.
- `cas.authn.attribute-repository.rest[0].basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.attribute-repository.rest[0].basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.attribute-repository.rest[0].headers` (Optional; type `Map<String, String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.attribute-repository.rest[0].id` (Optional; type `String`): A value can be assigned to this field to uniquely identify this resolver.
- `cas.authn.attribute-repository.rest[0].maximum-retry-attempts` (Optional; type `int`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.attribute-repository.rest[0].method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.
- `cas.authn.attribute-repository.rest[0].order` (Optional; type `int`): The order of this attribute repository in the chain of repositories. Can be used to explicitly position this source in chain and affects merging strategies.
- `cas.authn.attribute-repository.rest[0].state` (Optional; type `AttributeRepositoryStates`; default `ACTIVE`): Whether attribute resolution based on this source is enabled. Available values are as follows: - `ACTIVE`: Active and enabled repository, and is invoked by default automatically. - `DISABLED`: Attribute repository is disabled and will not be used to resolve people and attributes. - `STANDBY`: Repository is in a semi-enabled state, waiting to be called only on-demand when explicitly asked for and will not be registered into the resolution plan.
- `cas.authn.attribute-repository.rest[0].username-attribute` (Optional; type `String`; default `username`): The attribute name that would be used to look up and determine the user id from the query map. The value linked to this attribute would be used as the username or subject by the attribute repository to pass on to the ultimate source to locate the user record.

The authenticating user id is passed in form of a request parameter under `username`. The response is expected to be a JSON map as such:

```json
{
  "name" : "JohnSmith",
  "age" : 29,
  "messages": ["msg 1", "msg 2", "msg 3"]
}
```

## Multitenancy

Configuration settings for REST attribute resolution can be specified in a multitenant environment. Please [review this guide](https://apereo.github.io/cas/development/multitenancy/Multitenancy-Tenant-Properties.html) for more information.
