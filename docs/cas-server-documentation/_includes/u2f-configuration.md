```properties
# cas.authn.mfa.u2f.rank=0
# cas.authn.mfa.u2f.name=
# cas.authn.mfa.u2f.order=

# Expiry of U2F device registration requests:
# cas.authn.mfa.u2f.expire-registrations=30
# cas.authn.mfa.u2f.expire-registrations-time-unit=SECONDS

# Expiry of U2F devices since registration, independent of last time used:
# cas.authn.mfa.u2f.expire-devices=30
# cas.authn.mfa.u2f.expire-devices-time-unit=DAYS
```

{% include {{ version }}/mfa-bypass-configuration.md configKey="cas.authn.mfa.u2f" %}

{% include {{ version }}/signing-encryption-configuration.md configKey="cas.authn.mfa.u2f" signingKeySize="512" encryptionKeySize="256" encryptionAlg="AES_128_CBC_HMAC_SHA_256" %}
