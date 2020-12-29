```properties
# cas.authn.mfa.trusted.device-fingerprint.componentSeparator=@  

# cas.authn.mfa.trusted.device-fingerprint.cookie.enabled=true
# cas.authn.mfa.trusted.device-fingerprint.cookie.order=1

# cas.authn.mfa.trusted.device-fingerprint.client-ip.enabled=true
# cas.authn.mfa.trusted.device-fingerprint.client-ip.order=2

# cas.authn.mfa.trusted.device-fingerprint.geolocation.enabled=false
# cas.authn.mfa.trusted.device-fingerprint.geolocation.order=4
```

{% include {{ version }}/cookie-configuration.md configKey="cas.authn.mfa.trusted.device-fingerprint.cookie" %}

The default cookie name is set to `MFATRUSTED` and the default maxAge is set to `2592000`.

{% include {{ version }}/signing-encryption.md configKey="cas.authn.mfa.trusted.device-fingerprint.cookie" signingKeySize="512" encryptionKeySize="256" encryptionAlg="AES_128_CBC_HMAC_SHA_256" %}
