### SAML2 Delegated Authentication Configuration

The following options are shared and apply when CAS is configured to delegate authentication
to an external SAML2 identity provider such as Azure AD or Amazon Cognito, etc:

```properties
# {{ include.configKey }}.keystore-password=
# {{ include.configKey }}.private-key-password=
# {{ include.configKey }}.keystore-path=
# {{ include.configKey }}.keystore-alias=

# {{ include.configKey }}.service-provider-entity-id=
# {{ include.configKey }}.service-provider-metadata-path=

# {{ include.configKey }}.certificate-name-to-append=

# {{ include.configKey }}.maximum-authentication-lifetime=3600
# {{ include.configKey }}.maximum-authentication-lifetime=300
# {{ include.configKey }}.destination-binding=urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect

# {{ include.configKey }}.identity-provider-metadata-path=

# {{ include.configKey }}.authn-context-class-ref[0]=
# {{ include.configKey }}.authn-context-comparison-type=
# {{ include.configKey }}.name-id-policy-format=
# {{ include.configKey }}.force-auth=false
# {{ include.configKey }}.passive=false

# {{ include.configKey }}.wants-assertions-signed=
# {{ include.configKey }}.wants-responses-signed=
# {{ include.configKey }}.all-signature-validation-disabled=false
# {{ include.configKey }}.sign-service-provider-metadata=false
# {{ include.configKey }}.principal-id-attribute=eduPersonPrincipalName
# {{ include.configKey }}.use-name-qualifier=true
# {{ include.configKey }}.attribute-consuming-service-index=
# {{ include.configKey }}.assertion-consumer-service-index=-1
# {{ include.configKey }}.provider-name=
# {{ include.configKey }}.name-id-policy-allow-create=TRUE|FALSE|UNDEFINED


# {{ include.configKey }}.sign-authn-request=false
# {{ include.configKey }}.sign-service-provider-logout-request=false
# {{ include.configKey }}.black-listed-signature-signing-algorithms[0]=
# {{ include.configKey }}.signature-algorithms[0]=
# {{ include.configKey }}.signature-reference-digest-methods[0]=
# {{ include.configKey }}.signature-canonicalization-algorithm=

# {{ include.configKey }}.requested-attributes[0].name=
# {{ include.configKey }}.requested-attributes[0].friendly-name=
# {{ include.configKey }}.requested-attributes[0].name-format=urn:oasis:names:tc:SAML:2.0:attrname-format:uri
# {{ include.configKey }}.requested-attributes[0].required=false

# {{ include.configKey }}.mapped-attributes[0].name=urn:oid:2.5.4.42
# {{ include.configKey }}.mapped-attributes[0].mapped-as=displayName

# {{ include.configKey }}.message-store-factory=org.pac4j.saml.store.EmptyStoreFactory
```


Examine the generated metadata after accessing the CAS login screen to ensure all
ports and endpoints are correctly adjusted. Finally, share the CAS SP metadata
with the delegated identity provider and register CAS as an authorized relying party.
