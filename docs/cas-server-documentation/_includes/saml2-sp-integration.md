### {{ include.configKey | camelcase }} Configuration

The settings defined for each service provider simply attempt to automate the creation of
SAML service definition and nothing more. If you find the applicable settings lack in certain areas, it 
is best to fall back onto the native configuration strategy for registering
SAML service providers with CAS which would depend on your service registry of choice.

The SAML2 service provider supports the following settings:

| Name                  |  Description
|-----------------------|---------------------------------------------------------------------------
| `metadata`            | Location of metadata for the service provider (i.e URL, path, etc)
| `name`                | The name of the service provider registered in the service registry.
| `description`         | The description of the service provider registered in the service registry.
| `name-id-attribute`     | Attribute to use when generating name ids for this service provider.
| `name-id-format`        | The forced NameID Format identifier (i.e. `urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress`).
| `attributes`          | Attributes to release to the service provider, which may virtually be mapped and renamed.
| `signature-location`   | Signature location to verify metadata.
| `entity-ids`           | List of entity ids allowed for this service provider.
| `sign-responses`       | Indicate whether responses should be signed. Default is `true`.
| `sign-assertions`      | Indicate whether assertions should be signed. Default is `false`.

The only required setting that would activate the automatic configuration for a 
service provider is the presence and definition of metadata. All other settings are optional.

The following options apply equally to SAML2 service provider integrations, given the provider's *configuration key*:

```properties
# {{ include.configKey }}.metadata=/etc/cas/saml/metadata.xml
# {{ include.configKey }}.name=SP Name
# {{ include.configKey }}.description=SP Integration
# {{ include.configKey }}.name-id-attribute=mail
# {{ include.configKey }}.name-id-format=
# {{ include.configKey }}.signature-location=
# {{ include.configKey }}.attributes=
# {{ include.configKey }}.entity-ids=
# {{ include.configKey }}.sign-responses=
# {{ include.configKey }}.sign-assertions=
```

**Note**: For InCommon and other metadata aggregates, multiple entity ids can be specified to
filter [the InCommon metadata](https://spaces.internet2.edu/display/InCFederation/Metadata+Aggregates). EntityIds
can be regular expression patterns and are mapped to
CAS' `serviceId` field in the registry. The signature location MUST BE the public key used to sign the metadata.
