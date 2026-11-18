# HTTP/HTTPS - SAML2 Metadata Management

Source: https://apereo.github.io/cas/development/installation/Configuring-SAML2-DynamicMetadata-HTTP.html

A metadata location for a SAML service definition may download SAML2 metadata from http or https URLs. Multiple locations may be comma-separated.

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp",
  "name" : "SAMLService",
  "id" : 1,
  "description" : "A Groovy-based metadata resolver",
  "metadataLocation" : "https://sp.example.org/saml2/metadata"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.saml-idp.metadata.http.force-metadata-refresh` (Optional; type `Boolean`; default `true`): Forcefully download and fetch metadata files form URL sources and disregard any cached copies of the metadata.
- `cas.authn.saml-idp.metadata.http.metadata-backup-location` (Optional; type `String`): Directory location where downloaded SAML metadata is cached as backup files. If left undefined, the directory is calculated off of the metadata location on disk when specified. The directory location should also support and be resolvable via Spring expression language.
- `cas.authn.saml-idp.metadata.http.metadata-backup-max-age` (Optional; type `String`; default `P7D`)

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).
