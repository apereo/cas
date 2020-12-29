{% include {{ version }}/rest-configuration.md configKey="cas.authn.saml-idp.metadata.rest" %}

```properties
# cas.authn.saml-idp.metadata.rest.idp-metadata-enabled=true
```

{% include {{ version }}/signing-encryption-configuration.md configKey="cas.authn.saml-idp.metadata.rest" signingKeySize="512" encryptionKeySize="256" encryptionAlg="AES_128_CBC_HMAC_SHA_256" %}
