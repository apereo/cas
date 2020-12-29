{% include {{ version }}/mongodb-configuration.md configKey="cas.authn.saml-idp.metadata" %}

```properties
# cas.authn.saml-idp.metadata.mongo.idp-metadata-collection=saml-idp-metadata
```

{% include {{ version }}/signing-encryption.md configKey="cas.authn.saml-idp.metadata.mongo" signingKeySize="512" encryptionKeySize="256" encryptionAlg="AES_128_CBC_HMAC_SHA_256" %}
