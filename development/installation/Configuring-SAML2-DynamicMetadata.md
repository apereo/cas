# SAML2 Metadata Management

Source: https://apereo.github.io/cas/development/installation/Configuring-SAML2-DynamicMetadata.html

The following CAS endpoints handle the generation of SAML2 metadata:

- `/idp/metadata`
- `/idp/metadata/signingCertificate`
- `/idp/metadata/encryptionCertificate`

This endpoint will display the CAS IdP SAML2 metadata upon receiving a GET request. If metadata is already available and generated, it will be displayed. If metadata is absent, one will be generated automatically. The metadata is served as `application/samlmetadata+xml` to clients that ask for that media type in their `Accept` header, and as `text/xml` otherwise. Responses carry an `ETag`, so a client that already holds the current metadata and sends it back in `If-None-Match` receives `304 Not Modified` without the document, and clients are asked to revalidate before reusing it. CAS configuration below dictates where metadata files/keys will be generated and stored.

Note that the endpoint can accept a `service` parameter either by entity id or numeric identifier. This parameter is matched against the CAS service registry allowing the endpoint to calculate and combine any identity provider metadata overrides that may have been specified.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.saml-idp.metadata.core.cache-expiration` (Optional; type `String`; default `PT24H`): How long should metadata be cached.
- `cas.authn.saml-idp.metadata.core.cache-maximum-size` (Optional; type `Long`; default `10000`): Specifies the maximum number of entries the cache may contain. Note that the cache **may evict an entry before this limit is exceeded or temporarily exceed the threshold while evicting**. As the cache size grows close to the maximum, the cache evicts entries that are less likely to be used again. For example, the cache may evict an entry because it hasn't been used recently or very often.
- `cas.authn.saml-idp.metadata.core.certificate-algorithm` (Optional; type `String`; default `SHA512withRSA`): The algorithm type/name that is used when generating certificates for the SAML2 identity provider. This setting is only relevant when artifacts needs to be generated.
- `cas.authn.saml-idp.metadata.core.fail-fast` (Optional; type `Boolean`; default `true`): Whether invalid metadata should eagerly fail quickly on startup once the resource is parsed.
- `cas.authn.saml-idp.metadata.core.failure-cache-expiration` (Optional; type `String`; default `PT1M`)
- `cas.authn.saml-idp.metadata.core.key-size` (Optional; type `Integer`; default `4096`): This is the key size that is used when generating the initial keypair that would hold the private/public key for the SAML2 metadata. This setting is only relevant when artifacts needs to be generated.
- `cas.authn.saml-idp.metadata.core.maximum-cache-duration` (Optional; type `String`; default `P1D`)
- `cas.authn.saml-idp.metadata.core.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When fetching metadata over HTTP, such as from URLs or a metadata query server, this setting controls the number of **retry attempts** that CAS should execute when the request fails. Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.saml-idp.metadata.core.minimum-cache-duration` (Optional; type `String`; default `PT1M`)
- `cas.authn.saml-idp.metadata.core.require-valid-metadata` (Optional; type `Boolean`; default `true`): Whether valid metadata is required.
- `cas.authn.saml-idp.metadata.core.slo-service-post-binding-enabled` (Optional; type `Boolean`; default `true`): Whether metadata generation process should support SLO service POST binding.
- `cas.authn.saml-idp.metadata.core.slo-service-redirect-binding-enabled` (Optional; type `Boolean`; default `true`): Whether metadata generation process should support SLO service REDIRECT binding.
- `cas.authn.saml-idp.metadata.core.sso-service-post-binding-enabled` (Optional; type `Boolean`; default `true`): Whether metadata generation process should support SSO service POST binding.
- `cas.authn.saml-idp.metadata.core.sso-service-post-simple-sign-binding-enabled` (Optional; type `Boolean`; default `true`): Whether metadata generation process should support SSO service POST SimpleSign binding.
- `cas.authn.saml-idp.metadata.core.sso-service-redirect-binding-enabled` (Optional; type `Boolean`; default `true`): Whether metadata generation process should support SSO service REDIRECT binding.
- `cas.authn.saml-idp.metadata.core.sso-service-soap-binding-enabled` (Optional; type `Boolean`; default `true`): Whether metadata generation process should support SSO service SOAP binding.

Identity provider metadata, keys and certificates are cached in memory once loaded and are fetched again from their storage once the cache entry expires, regardless of how often they are used, so a key rotation in a shared metadata store reaches every CAS node within that period.

You may use [this service](https://www.samltool.com/idp_metadata.php) to experiment with the metadata generation process and produce an example metadata for review and study.

## Metadata Management

Identity provider metadata can also be managed using the following strategies.

### File System

SAML2 identity provider metadata by default is generated on disk. The contents of the generated or found artifacts such as signing and/or encryption keys may be encrypted via CAS configuration security [outlined here](https://apereo.github.io/cas/development/configuration/Configuration-Properties-Security-CAS.html).

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.saml-idp.metadata.file-system.location` (Required; type `String`; default `file:/etc/cas/saml`): Directory location of SAML metadata and signing/encryption keys. This directory will be used to hold the configuration files.
- `cas.authn.saml-idp.metadata.file-system.change-detection-interval` (Optional; type `String`; default `PT5S`)
- `cas.authn.saml-idp.metadata.file-system.sign-metadata` (Optional; type `Boolean`; default `false`): Whether metadata generated on disk should be digitally signed. Signing operations use the saml2 identity provider's signing certificate and signing key.

#### Per Service

Identity provider metadata, certificates and keys can also be defined on a per-service basis to override the global defaults. Metadata artifacts that would be applicable to a specific service definition and managed via the file system need to be stored in a directory location named after the service definition’s name and numeric identifier inside the canonical metadata directory. For example, if global metadata artifacts are managed on disk at `/etc/cas/config/saml/metadata`, then metadata applicable to a service definition whose name is configured as `SampleService` with an id of `1000` are expected to be found at `/etc/cas/config/saml/metadata/SampleService-1000`.

Alternatively, a SAML2 service provider can be directly instructed to locate identity provider metadata from a designated location on disk. This can be useful in scenarios where you want to rotate your signing and encryption keys of your SAML2 identity provider metadata and wish to gradually allow service providers to pick up the new metadata:

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp",
  "name" : "SAMLService",
  "id" : 1,
  "metadataLocation" : "https://url/to/metadata.xml",
  "idpMetadataLocation" : "file:/path/to/idp/metadata/directory"
}
```

### Advanced

Service provider or identity provider metadata can also be managed using any one of the following strategies.

| Storage | Description |
| --- | --- |
| Metadata Query Protocol | [See this guide](https://apereo.github.io/cas/development/installation/Configuring-SAML2-DynamicMetadata-MDQ.html). |
| HTTP/HTTPS | [See this guide](https://apereo.github.io/cas/development/installation/Configuring-SAML2-DynamicMetadata-HTTP.html). |
| REST | [See this guide](https://apereo.github.io/cas/development/installation/Configuring-SAML2-DynamicMetadata-REST.html). |
| Git | [See this guide](https://apereo.github.io/cas/development/installation/Configuring-SAML2-DynamicMetadata-Git.html). |
| MongoDb | [See this guide](https://apereo.github.io/cas/development/installation/Configuring-SAML2-DynamicMetadata-MongoDb.html). |
| Redis | [See this guide](https://apereo.github.io/cas/development/installation/Configuring-SAML2-DynamicMetadata-Redis.html). |
| JPA | [See this guide](https://apereo.github.io/cas/development/installation/Configuring-SAML2-DynamicMetadata-JPA.html). |
| Groovy | [See this guide](https://apereo.github.io/cas/development/installation/Configuring-SAML2-DynamicMetadata-Groovy.html). |
| Amazon S3 | [See this guide](https://apereo.github.io/cas/development/installation/Configuring-SAML2-DynamicMetadata-AmazonS3.html). |
| DynamoDb | [See this guide](https://apereo.github.io/cas/development/installation/Configuring-SAML2-DynamicMetadata-DynamoDb.html). |
| Google Cloud Storage | [See this guide](https://apereo.github.io/cas/development/installation/Configuring-SAML2-DynamicMetadata-GCP-Storage.html). |

## SAML Services

Please [see this guide](https://apereo.github.io/cas/development/services/SAML2-Service-Management.html) to learn more about how to configure SAML2 service providers.
