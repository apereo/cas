---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# WebFinger Issuer Discovery - OpenID Connect Authentication

OpenID Provider Issuer discovery is the process of determining the 
location of the OpenID Connect Provider. Issuer discovery is optional; if a Relying Party 
knows the OP's Issuer location through an out-of-band mechanism, it can skip this step.

Issuer discovery requires the following information to make a discovery request:

| Parameter  | Description                                                                                                        |
|------------|--------------------------------------------------------------------------------------------------------------------|
| `resource` | Required. Identifier for the target End-User that is the subject of the discovery request.                         |
| `host`     | Server where a WebFinger service is hosted.                                                                        |
| `rel`      | URI identifying the type of service whose location is being requested:`http://openid.net/specs/connect/1.0/issuer` |

To start discovery of OpenID endpoints, the End-User supplies an Identifier to 
the Relying Party. The RP applies normalization rules to the Identifier to
determine the Resource and Host. Then it makes an HTTP `GET` request to the CAS 
WebFinger endpoint with the `resource` and `rel` parameters to obtain 
the location of the requested service. The Issuer location **MUST** be returned in the WebFinger response as the value 
of the `href` member of a links array element with `rel` member value `http://openid.net/specs/connect/1.0/issuer`.

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

To determine the correct issuer, resources that are provided to 
the `webfinger` discovery endpoint using the `acct` URI scheme
can be located and fetched using external user repositories via `email` or `username`.

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>The default repository implementation will
echo back the provided email or username, etc as it is <strong>ONLY</strong> relevant for demo/testing purposes.</p></div>

The following user-info repository choices are available for configuration and production use.

### Groovy UserInfo Repository

The task of locating accounts linked to webfinger resources can be handled 
using an external Groovy script whose outline would match the following:

```groovy
def findByUsername(Object[] args) {
    def username = args[0]
    def logger = args[1]
    return [username: username]
}

def findByEmailAddress(Object[] args) {
    def email = args[0]
    def logger = args[1]
    return [email: email]
}
```

The expected return value from the script is a `Map` that contains 
key-value objects, representing user account details. An empty `Map`
would indicate the absence of the user record, leading to a `404` 
response status back to the relying party.

{% include_cached casproperties.html properties="cas.authn.oidc.webfinger.userInfo.groovy" %}

### REST UserInfo Repository

The REST repository allows the CAS server to reach to a remote REST 
endpoint via the configured HTTP method to fetch user account information.

Query data is passed via either `email` or `username` HTTP headers. 
The response that is returned must be accompanied by a `200`
status code where the body should contain `Map` representing the 
user account information. All other responses will lead to a `404` 
response status back to the relying party.

{% include_cached casproperties.html properties="cas.authn.oidc.webfinger.user-info.rest" %}

### Custom UserInfo Repository

It is possible to design and inject your own version of webfinger user repositories into CAS. First, you will need to design
a `@AutoConfiguration` class to contain your own `OidcWebFingerUserInfoRepository` implementation:

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

Your configuration class needs to be registered with CAS. [See this guide](../configuration/Configuration-Management-Extensions.html) for better details.
