### Attribute Types

In order to construct the final authenticated principal, CAS may be configured to use the following
strategies when collecting principal attributes:

| Type                 | Description
|----------------------|------------------------------------------------------------------------------------------------
| `CAS`                | Use attributes provided by CAS' own attribute resolution mechanics and repository.
| `WSFED`              | Use attributes provided by the delegated WS-Fed instance.
| `BOTH`               | Combine both the above options, where CAS attribute repositories take precedence over WS-Fed.

```properties
# {{ include.configKey }}.identity-provider-url=https://adfs.example.org/adfs/ls/
# {{ include.configKey }}.identity-provider-identifier=https://adfs.example.org/adfs/services/trust
# {{ include.configKey }}.relying-party-identifier=urn:cas:localhost
# {{ include.configKey }}.signing-certificate-resources=classpath:adfs-signing.crt
# {{ include.configKey }}.identity-attribute=upn

# {{ include.configKey }}.attributes-type=WSFED
# {{ include.configKey }}.tolerance=10000
# {{ include.configKey }}.attribute-resolver-enabled=true
# {{ include.configKey }}.auto-redirect=true
# {{ include.configKey }}.name=
# {{ include.configKey }}.attribute-mutator-script.location=file:/etc/cas/config/wsfed-attr.groovy

# {{ include.configKey }}.principal.principal-attribute=
# {{ include.configKey }}.principal.return-null=false

# Private/Public keypair used to decrypt assertions, if any.
# {{ include.configKey }}.encryption-private-key=classpath:private.key
# {{ include.configKey }}.encryption-certificate=classpath:certificate.crt
# {{ include.configKey }}.encryption-private-key-password=NONE
```

{% include {{ version }}/signing-encryption.md configKey="cas.authn.wsfed[0].cookie" signingKeySize="512" encryptionKeySize="256" encryptionAlg="AES_128_CBC_HMAC_SHA_256" %}

