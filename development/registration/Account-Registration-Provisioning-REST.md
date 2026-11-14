# Account (Self-Service) Registration - REST Provisioning

Source: https://apereo.github.io/cas/development/registration/Account-Registration-Provisioning-REST.html

Account registration requests can be submitted to an external REST API via a `POST` that is responsible for managing and storing the account in the appropriate systems of record.

The following settings and properties are available from the CAS configuration catalog:

- `cas.account-registration.provisioning.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.account-registration.provisioning.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.account-registration.provisioning.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.account-registration.provisioning.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.account-registration.provisioning.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.

The expected response status code form the API call is `200`. The API request body will contain the account registration request as such:

```json
{
  "@class":"org.apereo.cas.acct.AccountRegistrationRequest",
  "properties":{
    "@class":"java.util.LinkedHashMap",
    "username": "casuser",
    "field1": "value1"
  }
}
```
