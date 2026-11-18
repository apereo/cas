# SAML2 Delegated Authentication - FileSystem Service Provider Metadata

Source: https://apereo.github.io/cas/development/integration/Delegate-Authentication-SAML2-Metadata-FileSystem.html

SAML2 metadata for CAS as the SAML2 service provider is typically managed on disk, and generated on startup if the metadata file is not found. Future and subsequent changes to this metadata file, if necessary, must be handled manually and the file might need to be curated and edited to fit your purposes. This is the default option.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pac4j.saml[0].metadata.service-provider.file-system.location` (Required; type `String`): Location of the SP metadata to use and generate on the file system. If the metadata file already exists, it will be ignored and reused. Location of the SP metadata to use and generate on the file system. If the metadata file already exists, it will be ignored and reused.
