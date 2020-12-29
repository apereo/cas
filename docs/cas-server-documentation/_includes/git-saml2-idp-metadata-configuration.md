```properties
# cas.authn.saml-idp.metadata.git.idp-metadata-enabled=true
```

{% include {{ version }}/git-configuration.md configKey="cas.authn.saml-idp.metadata" %}

{% include {{ version }}/signing-encryption-configuration.md configKey="cas.authn.saml-idp.metadata.git" signingKeySize="512" encryptionKeySize="256" encryptionAlg="AES_128_CBC_HMAC_SHA_256" %}
