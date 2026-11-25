# WebFinger Issuer Discovery - OpenID Connect Authentication

Source: https://apereo.github.io/cas/development/authentication/OIDC-Authentication-WebFinger.html

OpenID Provider Issuer discovery is the process of determining the location of the OpenID Connect Provider. Issuer discovery is optional; if a Relying Party knows the OP’s Issuer location through an out-of-band mechanism, it can skip this step.

Issuer discovery requires the following information to make a discovery request:

| Parameter | Description |
| --- | --- |
| `resource` | Required. Identifier for the target End-User that is the subject of the discovery request. |
| `host` | Server where a WebFinger service is hosted. |
| `rel` | URI identifying the type of service whose location is being requested: `http://openid.net/specs/connect/1.0/issuer` |

To start discovery of OpenID endpoints, the End-User supplies an Identifier to the Relying Party. The RP applies normalization rules to the Identifier to determine the Resource and Host. Then it makes an HTTP `GET` request to the CAS WebFinger endpoint with the `resource` and `rel` parameters to obtain the location of the requested service. The Issuer location **MUST** be returned in the WebFinger response as the value of the `href` member of a links array element with `rel` member value `http://openid.net/specs/connect/1.0/issuer`.

Example invocation of the `webfinger` endpoint follows:

```bash
curl https://sso.example.org/cas/oidc/.well-known/webfinger?resource=acct:casuser@somewhere.example.org
```

The expected response shall match the following example:

```json
{
  "subject": "acct:casuser@somewhere.example.org",
  "links": [
    {
      "rel": "http://openid.net/specs/connect/1.0/issuer",
      "href": "https://sso.example.org/cas/oidc/"
    }
  ]
}
```

## WebFinger Resource UserInfo

To determine the correct issuer, resources that are provided to the `webfinger` discovery endpoint using the `acct` URI scheme can be located and fetched using external user repositories via `email` or `username`.

> **Usage Warning!**
>
> The default repository implementation will echo back the provided email or username, etc as it is **ONLY** relevant for demo/testing purposes.

The following user-info repository choices are available for configuration and production use.

### Groovy UserInfo Repository

The task of locating accounts linked to webfinger resources can be handled using an external Groovy script whose outline would match the following:

```groovy
def findByUsername(Object[] args) {
    def (username,logger) = args
    return [username: username]
}

def findByEmailAddress(Object[] args) {
    def (email,logger) = args
    return [email: email]
}
```

The expected return value from the script is a `Map` that contains key-value objects, representing user account details. An empty `Map` would indicate the absence of the user record, leading to a `404` response status back to the relying party.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.webfinger.user-info.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).

### REST UserInfo Repository

The REST repository allows the CAS server to reach to a remote REST endpoint via the configured HTTP method to fetch user account information.

Query data is passed via either `email` or `username` HTTP headers. The response that is returned must be accompanied by a `200` status code where the body should contain `Map` representing the user account information. All other responses will lead to a `404` response status back to the relying party.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.webfinger.user-info.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.oidc.webfinger.user-info.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.oidc.webfinger.user-info.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.oidc.webfinger.user-info.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.oidc.webfinger.user-info.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.oidc.webfinger.user-info.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.

### Custom UserInfo Repository

It is possible to design and inject your own version of webfinger user repositories into CAS. First, you will need to design a `@AutoConfiguration` class to contain your own `OidcWebFingerUserInfoRepository` implementation:

```java
@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CustomWebFingerUserInfoConfiguration {

    @Bean
    public OidcWebFingerUserInfoRepository oidcWebFingerUserInfoRepository() {
        ...
    }
}
```

Your configuration class needs to be registered with CAS. [See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) for better details.
