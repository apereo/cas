# REST Acceptable Usage Policy

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-AUP-REST.html

CAS can be configured to use a REST API as the storage mechanism. Upon accepting the policy, the API is passed `username` parameter via `POST` who has accepted the policy along with the active `locale`. A `service` parameter is also passed as the indicator of the target application, if one is available. The expected response status code is `200`.

The condition to determine whether the policy is accepted by the user is controlled typically via a configurable attribute. If this attribute does not exist or CAS decides that there is not enough evidence to determine an accepted status from this attribute, the API endpoint at `${REST_ENDPOINT}/status` is contacted via a `GET` request to determine the policy acceptance status. As before, this API is passed `username`, `locale` and `service` (if available) parameters and the expected response status code, noting an accepted status, is `200`.

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-aup-rest"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.acceptable-usage-policy.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.acceptable-usage-policy.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.acceptable-usage-policy.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.acceptable-usage-policy.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.acceptable-usage-policy.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.acceptable-usage-policy.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.

## Policy Terms

The API endpoint at `${REST_ENDPOINT}/policy` will be invoked by CAS via a `GET` to fetch the appropriate policy terms. The API is passed `username`, `locale` and `service` parameters and the expected response status code is `200`. The response output body is expected to be an instance of `AcceptableUsagePolicyTerms` as such:

```json
{
  "@class": "org.apereo.cas.aup.AcceptableUsagePolicyTerms",
  "code": "screen.aup.policyterms.some.key",
  "defaultText": "Default policy text"
}
```

The provided `code` may point to a language key in the CAS message bundles. The text attached to this key in the relevant bundle is ultimately pulled out and processed by the CAS UI.
