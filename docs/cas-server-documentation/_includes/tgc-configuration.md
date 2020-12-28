{% include {{ version }}/cookie-configuration.md configKey="cas.tgc" %}

```properties
# cas.tgc.pin-to-session=true
# cas.tgc.remember-me-max-age=P14D
# cas.tgc.auto-configure-cookie-path=true
```

{% include {{ version }}/signing-encryption.md configKey="cas.tgc" signingKeySize="512" encryptionKeySize="256" encryptionAlg="AES_128_CBC_HMAC_SHA_256" %}
