# SAML2 Delegated Authentication - Metadata Management

Source: https://apereo.github.io/cas/development/integration/Delegate-Authentication-SAML2-Metadata.html

In the event that CAS is configured to delegate authentication to an external identity provider, the service provider (CAS) metadata as well as the identity provider metadata automatically become available at the following endpoints:

| Endpoint | Description |
| --- | --- |
| `/sp/metadata` | Displays the service provider (CAS) metadata. Works well if there is only one SAML2 IdP is defined. |
| `/sp/idp/metadata` | Displays the identity provider metadata. Works well if there is only one SAML2 IdP is defined. Accepts a `force=true` parameter to reload the identity provider’s metadata. |
| `/sp/{clientName}/metadata` | Displays the service provider metadata for the requested client name. |
| `/sp/{clientName}/idp/metadata` | Displays the identity provider metadata for the requested client name. Accepts a `force=true` parameter to reload the identity provider’s metadata |

Note that you can use more than one external identity provider with CAS, where each integration may be done with a different set of metadata and keys for CAS acting as the service provider. Each integration (referred to as a client, since CAS itself becomes a client of the identity provider) may be given a name optionally.

Remember that the service provider (CAS) metadata is automatically generated once you access the above endpoints or view the CAS login screen. This is required because today, generating the metadata requires access to the HTTP request/response. In the event that metadata cannot be resolved, a status code of `406 - Not Acceptable` is returned.

## Strategies

SAML2 metadata for both the delegated identity provider as well as the (CAS) service provider can managed via the following settings.

**Identity Provider**

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-path` (Required; type `String`): The metadata location of the identity provider that is to handle authentications. The location can be specified as a direct absolute path to the metadata file or it may also be a URL to the identity provider's metadata endpoint.
- `cas.authn.pac4j.saml[0].metadata.identity-provider-metadata-aggregate` (Optional; type `boolean`): Indicate whether the identity provider metadata is an aggregate and contains more than one IDP entity. Doing so will allow CAS to resolve and load the identity provider entities from the aggregate and register them individually with the runtime. The resolution process will attempt to use the `UIInfo` element for each identity provider entity to extract things like Display Name, etc.

If you intend to support multiple identity providers via a metadata aggregate, please [review this guide](https://apereo.github.io/cas/development/integration/Delegate-Authentication-SAML2-Metadata-Aggregate.html).

**Service Provider**

SAML2 metadata for CAS as the service provider can be managed via the following strategies.

| Option | Reference |
| --- | --- |
| File System | [See this guide](https://apereo.github.io/cas/development/integration/Delegate-Authentication-SAML2-Metadata-FileSystem.html). |
| JDBC | [See this guide](https://apereo.github.io/cas/development/integration/Delegate-Authentication-SAML2-Metadata-JDBC.html). |
| MongoDb | [See this guide](https://apereo.github.io/cas/development/integration/Delegate-Authentication-SAML2-Metadata-MongoDb.html). |
| Amazon S3 | [See this guide](https://apereo.github.io/cas/development/integration/Delegate-Authentication-SAML2-Metadata-AmazonS3.html). |
