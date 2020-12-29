
{% include {{ version }}/aws-configuration.md configKey="cas.authn.saml-idp.metadata.amazon-s3" %}

{% include {{ version }}/signing-encryption-configuration.md configKey="cas.authn.saml-idp.metadata.amazon-s3" signingKeySize="512" encryptionKeySize="256" encryptionAlg="AES_128_CBC_HMAC_SHA_256" %}

```properties
# cas.authn.saml-idp.metadata.amazon-s3.bucket-name=saml-sp-bucket
# cas.authn.saml-idp.metadata.mongo.idp-metadata-bucket-name=saml-idp-bucket
```
