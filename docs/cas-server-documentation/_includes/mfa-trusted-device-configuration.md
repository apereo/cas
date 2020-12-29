```properties
# cas.authn.mfa.trusted.authentication-context-attribute=isFromTrustedMultifactorAuthentication
# cas.authn.mfa.trusted.device-registration-enabled=true
# cas.authn.mfa.trusted.key-generator-type=DEFAULT|LEGACY
```

The following strategies can be used to generate keys for trusted device records:

| Type                 | Description
|----------------------|------------------------------------------------------------------------------------------------
| `DEFAULT`            | Uses a combination of the username, device name and device fingerprint to generate the device key.
| `LEGACY`             | Deprecated. Uses a combination of the username, record date and device fingerprint to generate the device key.

{% include {{ version }}/signing-encryption.md configKey="cas.authn.mfa.trusted" signingKeySize="512" encryptionKeySize="256" encryptionAlg="AES_128_CBC_HMAC_SHA_256" %}
