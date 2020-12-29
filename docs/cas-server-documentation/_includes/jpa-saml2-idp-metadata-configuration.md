{% include {{ version }}/rdbms-configuration.md configKey="cas.authn.saml-idp.metadata.jpa" %}

```properties
# cas.authn.saml-idp.metadata.jpa.idp-metadata-enabled=true
```

{% include {{ version }}/signing-encryption.md configKey="cas.authn.saml-idp.metadata.jpa" signingKeySize="512" encryptionKeySize="256" encryptionAlg="AES_128_CBC_HMAC_SHA_256" %}
