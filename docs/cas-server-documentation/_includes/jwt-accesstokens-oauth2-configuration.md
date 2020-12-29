
```properties
# cas.authn.oauth.access-token.create-as-jwt=false
# cas.authn.oauth.access-token.crypto.encryption-enabled=true
# cas.authn.oauth.access-token.crypto.signing-enabled=true
```

{% include {{ version }}/signing-encryption-configuration.md configKey="cas.authn.oauth.access-token" signingKeySize="512" encryptionKeySize="256" encryptionAlg="AES_128_CBC_HMAC_SHA_256" %}
