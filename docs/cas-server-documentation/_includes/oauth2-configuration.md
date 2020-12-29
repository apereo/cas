{% include {{ version }}/signing-encryption-configuration.md configKey="cas.authn.oauth" signingKeySize="512" encryptionKeySize="256" encryptionAlg="AES_128_CBC_HMAC_SHA_256" %}

```properties
# cas.authn.oauth.replicate-sessions=false 
# cas.authn.oauth.grants.resource-owner.require-service-header=true
# cas.authn.oauth.user-profile-view-type=NESTED|FLAT
```

{% include {{ version }}/casclient-configuration.md %}
