# Remote Authentication

Source: https://apereo.github.io/cas/development/authentication/Remote-Authentication.html

This authentication strategy uses the HTTP request’s properties (address, cookie, etc) to transparently authenticate a user.

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-generic-remote-webflow"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.remote.ip-address-range` (Required; type `String`): The authorized network address to allow for authentication. This approach allows for transparent authentication achieved within a large corporate network without the need to manage certificates, etc.
- `cas.authn.remote.name` (Optional; type `String`): The name of the authentication handler.
- `cas.authn.remote.order` (Optional; type `Integer`): Order of the authentication handler in the chain.

## Remote Address

This authentication strategy uses the request’s remote address to transparently authenticate a user, having verified the address against a range of configured IP addresses. The mechanics of this approach are very similar to X.509 certificate authentication, but trust is instead placed on the client internal network address.

The benefit of this approach is that transparent authentication is achieved within a large corporate network without the need to manage certificates.

> **Be Careful**
>
> Keep in mind that this authentication mechanism should only be enabled for internal network clients with relatively static IP addresses.

### Caveats

This method of authentication assumes internal clients will be hitting the CAS server directly and not coming via a web proxy. In the event of the client using the web proxy the likelihood of the remote address lookup succeeding is reduced because to CAS the client address is that of the proxy server and not the client. Given that this form of CAS authentication would typically be deployed within an internal network this is generally not a problem.

## Remote Cookie

This method of authentication is able to accept a cookie value as a remote (shared) credential. Once the cookie is passed onto CAS and has been parsed and decoded, its value is used as the authenticated principal id.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.remote.cookie.crypto.encryption.key` (Required; type `String`): The encryption key is a string whose length is defined by the encryption key size setting.
- `cas.authn.remote.cookie.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.authn.remote.cookie.cookie-name` (Optional; type `String`): The name of the remote cookie that is passed onto CAS, usually set by a trusted third party. The cookie, when verified and decrypted, must indicate the trusted remote principal id that CAS should use for authentication.
- `cas.authn.remote.cookie.crypto.alg` (Optional; type `String`): The signing/encryption algorithm to use.
- `cas.authn.remote.cookie.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.authn.remote.cookie.crypto.encryption.key-size` (Optional; type `Integer`; default `512`): The encryption key size.
- `cas.authn.remote.cookie.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.authn.remote.cookie.crypto.strategy-type` (Optional; type `String`; default `ENCRYPT_AND_SIGN`): Control the cipher sequence of operations. The accepted values are: - `ENCRYPT_AND_SIGN`: Encrypt the value first, and then sign. - `SIGN_AND_ENCRYPT`: Sign the value first, and then encrypt.

**Signing & encryption**

This CAS feature is able to accept signing and encryption crypto keys. In most scenarios if keys are not provided, CAS will auto-generate them. The following instructions apply if you wish to manually and beforehand create the signing and encryption keys.

Note that if you are asked to create a [JWK](https://tools.ietf.org/html/rfc7517) of a certain size for the key, you are to use the following set of commands to generate the token:

```bash
wget https://raw.githubusercontent.com/apereo/cas/master/etc/jwk-gen.jar
java -jar jwk-gen.jar -t oct -s [size]
```

The outcome would be similar to:

```json
{
  "kty": "oct",
  "kid": "...",
  "k": "..."
}
```

The generated value for `k` needs to be assigned to the relevant CAS settings. Note that keys generated via the above algorithm are processed by CAS using the Advanced Encryption Standard (`AES`) algorithm which is a specification for the encryption of electronic data established by the U.S. National Institute of Standards and Technology.

---

By default, the cookie value is expected to be signed and encrypted using dedicated cipher keys when passed onto CAS, and CAS must use the same keys to decode and verify the cookie value.

If you wish to control how to the cookie value is decoded and parsed, you may do so via the following bean definition:

```java
@Bean
public CipherExecutor remoteCookieCipherExecutor() {
    return new MyCipherExecutor();
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
