```properties
# cas.authn.token.crypto.encryption-enabled=true
# cas.authn.token.crypto.signing-enabled=true
```

{% include {{ version }}/signing-encryption-configuration.md configKey="cas.authn.token" signingKeySize="512" encryptionKeySize="256" encryptionAlg="AES_128_CBC_HMAC_SHA_256" %}
