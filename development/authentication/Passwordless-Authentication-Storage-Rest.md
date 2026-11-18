# REST Passwordless Authentication Storage

Source: https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Storage-Rest.html

Look up passwordless user accounts, with their email address and phone number, by calling a REST endpoint you provide.

This strategy allows one design REST endpoints in charge of locating passwordless user records. A successful execution of the endpoint
would produce a response body similar to the following:

```json
{
  "@class" : "org.apereo.cas.api.PasswordlessUserAccount",
  "username" : "casuser",
  "email" : "cas@example.org",
  "phone" : "123-456-7890",
  "name" : "CASUser",        
  "multifactorAuthenticationEligible": "FALSE",  
  "delegatedAuthenticationEligible": "FALSE",  
  "requestPassword": false,
  "allowedDelegatedClients" : [ "java.util.ArrayList", [ "ClientName1" ] ],
  "attributes":{ "lastName" : ["...", "..."] }
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.passwordless.accounts.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.passwordless.accounts.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.passwordless.accounts.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.passwordless.accounts.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.passwordless.accounts.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.passwordless.accounts.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.
