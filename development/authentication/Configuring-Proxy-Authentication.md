# Proxy Authentication

Source: https://apereo.github.io/cas/development/authentication/Configuring-Proxy-Authentication.html

Proxy authentication support for CAS v1+ protocols is enabled by default, thus it is entirely a matter of CAS client configuration to leverage proxy authentication features.

> **Service Configuration**
>
> Note that each registered application in the registry must explicitly be configured to allow for proxy authentication. See [this guide](https://apereo.github.io/cas/development/services/Service-Management.html) to learn about registering services in the registry.

Disabling proxy authentication components is recommended for deployments that wish to strategically avoid proxy authentication as a matter of security policy.

## Use Case

One of the more common use cases of proxy authentication is the ability to obtain a ticket for a back-end [REST-based] service that is also protected by CAS. The scenario usually is:

- User is faced with application A which is protected by CAS.
- Application A on the backend needs to contact a service S to produce data.
- Service S itself is protected by CAS itself.

Because A contacts service S via a server-to-service method where no browser is involved, service S would not be able to recognize that an SSO session already exists. In these cases, application A needs to exercise proxying in order to obtain a proxy ticket for service S. The proxy ticket is passed to the relevant endpoint of service S so it can retrieve and validate it via CAS and finally produce a response.

The trace route may look like this:

1. Browser navigates to A.
2. A redirects to CAS.
3. CAS authenticates and redirects back to A with an `ST`.
4. A attempts to validate the `ST`, and asks for a `PGT`.
5. CAS confirms `ST` validation, and issues a proxy-granting ticket `PGT`.
6. A asks CAS to produce a `PT` for service S, supplying the `PGT` in its request.
7. CAS produces a PT for service S.
8. A contacts the service S endpoint, passing along `PT` in the request.
9. Service S attempts to validate the `PT` via CAS.
10. CAS validates the `PT` and produces a successful response.
11. Service S receives the response, and produces data for A.
12. A receives and displays the data in the browser.

See the [CAS Protocol](https://apereo.github.io/cas/development/protocol/CAS-Protocol.html) for more info.

## Handling SSL-enabled Proxy URLs

By default, CAS ships with a bundled HTTP client that is partly responsible to callback the URL for proxy authentication. Note that this URL need also be authorized by the CAS service registry before the callback can be made. [See this guide](https://apereo.github.io/cas/development/services/Service-Management.html) for more info.

If the callback URL is authorized by the service registry, and if the endpoint is under HTTPS and protected by an SSL certificate, CAS will also attempt to verify the validity of the endpoint’s certificate before it can establish a successful connection. If the certificate is invalid, expired, missing a step in its chain, self-signed or otherwise, CAS will fail to execute the callback.

The HTTP client of CAS does present a local trust store that is similar to that of the Java platform. It is recommended that this trust store be used to handle the management of all certificates that need to be imported into the platform to allow CAS to execute the callback URL successfully. While by default, the local trust store to CAS is empty, CAS will still utilize **both** the default and the local trust store. The local trust store should only be used for CAS-related functionality of course, and the trust store file can be carried over across CAS and Java upgrades, and certainly managed by the source control system that should host all CAS configuration.

The following settings and properties are available from the CAS configuration catalog:

- `cas.http-client.allow-local-urls` (Optional; type `Boolean`; default `true`): Whether CAS should accept local URLs. For example `http(s)://localhost/logout`.
- `cas.http-client.async-timeout` (Optional; type `String`; default `PT5S`): Indicates timeout for async operations.
- `cas.http-client.authority-validation-reg-ex-case-sensitive` (Optional; type `Boolean`; default `true`): Whether the regular expression specified with `#authorityValidationRegex` should be handled as case-sensitive (`true`) or case-insensitive (`false`). If no `#authorityValidationRegex` is set, this value does not have any effect.
- `cas.http-client.authority-validation-regex` (Optional; type `String`): If specified the regular expression will be used to validate the url's authority.
- `cas.http-client.connection-timeout` (Optional; type `String`; default `PT5S`): Connection timeout for all operations that reach out to URL endpoints.
- `cas.http-client.default-headers` (Optional; type `Map<String,String>`): The default headers to use for any HTTP connection. This is defined as map, where the key is the header name and the value is the header value that should be sent along with request.
- `cas.http-client.host-name-verifier` (Optional; type `String`; default `default`): Enable hostname verification when attempting to contact URL endpoints. May also be set to `none` to disable verification.
- `cas.http-client.proxy-host` (Optional; type `String`): Send requests via a proxy; define the hostname.
- `cas.http-client.proxy-port` (Optional; type `Integer`; default `0`): Send requests via a proxy; define the proxy port. Negative/zero values should deactivate the proxy configuration for the http client.
- `cas.http-client.response-timeout` (Optional; type `String`; default `PT5S`): Determines the timeout until arrival of a response from the opposite endpoint. A timeout value of zero is interpreted as an infinite timeout. Please note that response timeout may be unsupported by HTTP transports with message multiplexing.
- `cas.http-client.socket-timeout` (Optional; type `String`; default `PT5S`): Determines the default socket timeout value for I/O operations.
- `cas.http-client.truststore.file` (Optional; type `Resource`): The CAS local truststore resource to contain certificates to the CAS deployment. In the event that local certificates are to be imported into the CAS running environment, a local truststore is provided by CAS to improve portability of configuration across environments.
- `cas.http-client.truststore.psw` (Optional; type `String`; default `changeit`): The truststore password.
- `cas.http-client.truststore.type` (Optional; type `String`): Truststore type used to create a SSL context for http client.

## PGT in Validation Response

In situations where using `CAS20ProxyHandler` may be undesirable, such that invoking a callback url to receive the proxy granting ticket is not feasible, CAS may be configured to return the proxy-granting ticket id directly in the validation response. In order to successfully establish trust between the CAS server and the application, private/public key pairs are generated by the client application and then **the public key** distributed and configured inside CAS. CAS will use the public key to encrypt the proxy granting ticket id and will issue a new attribute `<proxyGrantingTicketId>` in the validation response, only if the service is authorized to receive it.

Note that the return of the proxy granting ticket id is only carried out by the CAS validation response, provided the client application issues a request to the `/p3/serviceValidate` endpoint (or `/p3/proxyValidate`). Other means of returning attributes to CAS, such as SAML1 will **not** support the additional returning of the proxy granting ticket.

> **Note** If CAS is configured to return the proxy-granting ticket id directly in the validation response, the `pgtIou` parameter is omitted from the response and no callback to the application is performed.

### Register Service

Once you have received the public key from the client application owner, it must be first registered inside the CAS server’s service registry. The service that holds the public key above must also be authorized to receive the PGT as an attribute for the given attribute release policy of choice.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "test",
  "id" : 1,
  "evaluationOrder" : 0,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "authorizedToReleaseProxyGrantingTicket" : true
  },
  "publicKey" : {
    "@class" : "org.apereo.cas.services.RegisteredServicePublicKeyImpl",
    "location" : "classpath:public.key",
    "algorithm" : "RSA"
  }
}
```

The keypair must be generated by the application itself that wishes to obtain the PGT. The public key is shared with CAS. The private key is used by the application to decrypt the PGT. Sample instructions to generate the keypair follow:

```bash
openssl genrsa -out private.key 4096
openssl rsa -pubout -in private.key -out public.key -inform PEM -outform DER
openssl pkcs8 -topk8 -inform PEM -outform DER -nocrypt -in private.key -out private.p8
```

Note that a large key size of `4096` may be required in order to allow CAS to encrypt lengthy proxy-granting tickets. Choosing a small key size will may prevent CAS to correctly encrypt the ticket as there is a limit to the lengths the encryption algorithm of a particular size can handle.

### Decrypt Proxy-Granting Ticket

Once the client application has received the `proxyGrantingTicket` id attribute in the CAS validation response, it can decrypt it via its own private key. Since the attribute is base64 encoded by default, it needs to be decoded first before decryption can occur. Here’s a sample code snippet:

```java
var attributes = new HashMap<>();
var encodedPgt = (String) attributes.get("proxyGrantingTicket");
var privateKey = ...
var cipher = Cipher.getInstance(privateKey.getAlgorithm());
var cred64 = decodeBase64(encodedPgt);
cipher.init(Cipher.DECRYPT_MODE, privateKey);
var cipherData = cipher.doFinal(cred64);
return new String(cipherData);
```
