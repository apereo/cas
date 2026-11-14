# JWKS Storage - OpenID Connect Authentication

Source: https://apereo.github.io/cas/development/authentication/OIDC-Authentication-JWKS-Storage.html

By default, a global keystore can be expected and defined via CAS properties as a path on the file system. The format of the keystore file is similar to the following:

```json
{
  "keys": [
    {
      "d": "...",
      "e": "AQAB",
      "use": "sig",
      "n": "...",
      "kty": "RSA",
      "kid": "cas",
      "state": 0
    }
  ]
}
```

The contents of the keystore may be encrypted via CAS configuration security [outlined here](https://apereo.github.io/cas/development/configuration/Configuration-Properties-Security-CAS.html).

> **Clustered Deployments**
>
> When deploying CAS in a cluster, you must make sure all CAS server nodes have access to and share an **identical and exact copy** of the keystore file. Keystore differences will lead to various validation failures and application integration issues.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.jwks.file-system.jwks-file` (Required; type `String`; default `file:/etc/cas/config/keystore.jwks`): Path to the JWKS file resource used to handle signing/encryption of authentication tokens. Contents of the keystore may be encrypted using the same encryption and security mechanism available for all other CAS configuration settings. The setting value here may also be defined in a raw format; that is, you may pass the actual contents of the keystore verbatim to this setting and CAS would load the keystore as an in-memory resource. This is relevant in scenarios where the setting source is external to CAS and has no support for file systems where the value is loaded on the fly from the source into this setting. Note that if the keystore files does not exist at the specified path, one will be generated for you.
- `cas.authn.oidc.jwks.file-system.watcher-enabled` (Optional; type `Boolean`; default `true`): Flag indicating whether a background watcher thread is enabled for the purposes of live reloading of keystore data file changes from disk.

The keystore is automatically watched and monitored by CAS for changes. As changes are detected, CAS will invalidate the cache and will reload the keystore once again.

## Advanced

The following alternative strategies can be used to generate, manage and storage a JSON Web keystore.

| Option | Reference |
| --- | --- |
| Groovy | [See this page](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-JWKS-Storage-Groovy.html). |
| JPA | [See this page](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-JWKS-Storage-JPA.html). |
| MongoDb | [See this page](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-JWKS-Storage-MongoDb.html). |
| REST | [See this page](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-JWKS-Storage-REST.html). |
| Custom | [See this page](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-JWKS-Storage-Custom.html). |
