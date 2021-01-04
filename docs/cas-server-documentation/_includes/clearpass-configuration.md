<div class="alert alert-warning"><strong>Usage Warning!</strong><p>ClearPass is turned off by default.
Think <strong>VERY CAREFULLY</strong> before turning on this feature, as it <strong>MUST</strong> be
the last resort in getting an integration to work...maybe not even then.</p></div>

```properties
# cas.clearpass.cache-credential=false
```

{% include {{ version }}/signing-encryption-configuration.md configKey="cas.clearpass" signingKeySize="512" encryptionKeySize="256" encryptionAlg="AES_128_CBC_HMAC_SHA_256" %}
