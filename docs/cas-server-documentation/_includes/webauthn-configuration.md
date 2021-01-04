
```properties
# cas.authn.mfa.web-authn.allowed-origins=
# cas.authn.mfa.web-authn.application-id=
# cas.authn.mfa.web-authn.relying-party-name=CAS WebAuthn 
# cas.authn.mfa.web-authn.relying-party-id=

# cas.authn.mfa.web-authn.display-name-attribute=displayName
# cas.authn.mfa.web-authn.allow-primary-authentication=false

# cas.authn.mfa.web-authn.allow-unrequested-extensions=false
# cas.authn.mfa.web-authn.allow-untrusted-attestation=false
# cas.authn.mfa.web-authn.validate-signature-counter=true
# cas.authn.mfa.web-authn.attestation-conveyance-preference=DIRECT|INDIRECT|NONE
# cas.authn.mfa.web-authn.trusted-device-metadata.location=

# cas.authn.mfa.web-authn.trusted-device-enabled=false

# cas.authn.mfa.web-authn.expire-devices=30
# cas.authn.mfa.web-authn.expire-devices-time-unit=DAYS
```   

{% include {{ version }}/mfa-bypass-configuration.md configKey="cas.authn.mfa.web-authn" %}

{% include {{ version }}/signing-encryption-configuration.md configKey="cas.authn.mfa.web-authn" signingKeySize="512" encryptionKeySize="256" encryptionAlg="AES_128_CBC_HMAC_SHA_256" %}
