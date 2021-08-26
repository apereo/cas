---
layout: default
title: CAS - SAML2 Service Management
category: Services
---

{% include variables.html %}

# SAML2 Services

SAML relying parties and services must be registered within the CAS service registry similar to the following example:

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp",
  "name" : "SAMLService",
  "id" : 10000003,
  "evaluationOrder" : 10,
  "metadataLocation" : "https://url/to/metadata.xml"
}
```

The following fields are available for SAML services:

| Field                                | Description
|--------------------------------------|------------------------------------------------------------------
| `metadataLocation`                   | Location of service metadata defined from system files, classpath, directories or URL resources.
| `metadataProxyLocation`              | Proxy endpoint (`https://proxy-address:8901`) to fetch service metadata from URL resources.
| `metadataSignatureLocation`          | Location of the metadata signing certificate/public key to validate the metadata which must be defined from system files or classpath. If defined, will enforce the `SignatureValidationFilter` validation filter on metadata.
| `metadataExpirationDuration`         | If defined, will expire metadata in the cache after the indicated duration which will force CAS to retrieve and resolve the metadata again.
| `requireSignedRoot`                  | Whether incoming metadata's root element is required to be signed. Default is `true`.
| `signUnsolicitedAuthnRequest`        | When dealing with Unsolicited SSO, determine whether the authentication request should be forcefully signed.
| `signAssertions`                     | Whether assertions should be signed. Default is `false`.
| `signResponses`                      | Whether responses should be signed. Default is `true`.
| `encryptionOptional`                 | Encrypt whenever possible (i.e a compatible key is found in the peer's metadata) or skip encryption otherwise. Default is `false`.
| `encryptAssertions`                  | Whether assertions should be encrypted. Default is `false`.
| `encryptAttributes`                  | Whether assertion attributes should be encrypted. Default is `false`.
| `encryptableAttributes`              | Set of attributes nominated for encryption, disqualifying others absent in this collection. Default (i.e. `*`) is to encrypt all once `encryptAttributes` is true.
| `requiredAuthenticationContextClass` | If defined, will specify the SAML authentication context class in the final response. If undefined, the authentication class will either be `urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified` or `urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport` depending on the SAML authentication request.
| `requiredNameIdFormat`               | If defined, will force the indicated Name ID format in the final SAML response.
| `skewAllowance`                      | If defined, indicates number of seconds used to skew authentication dates such as valid-from and valid-until elements, etc.
| `metadataCriteriaPattern`            | If defined, will force an entity id filter on the metadata aggregate based on the `PredicateFilter` to include/exclude specific entity ids based on a valid regex pattern.
| `metadataCriteriaDirection`          | If defined, will force an entity id filter on the metadata aggregate based on `PredicateFilter`. Allowed values are `INCLUDE`,`EXCLUDE`.
| `metadataCriteriaRoles`              | If defined, will allow the defined metadata roles (i.e. `SPSSODescriptor`, `IDPSSODescriptor`). Default is `SPSSODescriptor`.
| `metadataCriteriaRemoveEmptyEntitiesDescriptors` | Controls whether to keep entities descriptors that contain no entity descriptors. Default is `true`.
| `metadataCriteriaRemoveRolelessEntityDescriptors` | Controls whether to keep entity descriptors that contain no roles. Default is `true`.
| `attributeNameFormats` | Map that defines attribute name formats for a given attribute name to be encoded in the SAML response.
| `attributeFriendlyNames` | Map that defines attribute friendly names for a given attribute name to be encoded in the SAML response.
| `attributeValueTypes` | Map that defines the type of attribute values for a given attribute name.
| `nameIdQualifier` | If defined, will overwrite the `NameQualifier` attribute of the produced subject's name id.
| `logoutResponseBinding` | If defined, will overwrite the binding used to prepare logout responses for the service provider.
| `logoutResponseEnabled` | Control whether SAML2 logout responses should be generated and sent for this SAML2 service provider.
| `issuerEntityId` | If defined, will override the issue value with the given identity provider entity id. This may be useful in cases where CAS needs to maintain multiple identity provider entity ids.
| `assertionAudiences` | Comma-separated list of audience urls to include in the assertion, in the addition to the entity id.
| `subjectLocality` | If defined, will overwrite the `SubjectLocality` attribute of the SAML2 authentication statement.
| `serviceProviderNameIdQualifier` | If defined, will overwrite the `SPNameQualifier` attribute of the produced subject's name id.
| `skipValidatingAuthnRequest` | Skip validating the SAML2 authentication request and its signature in particular. Default is `false`.
| `skipGeneratingAssertionNameId` | Whether generation of a name identifier should be skipped for assertions. Default is `false`.
| `skipGeneratingTransientNameId` | Whether transient name identifier generation should be skipped. Default is `false`.
| `skipGeneratingSubjectConfirmationInResponseTo` | Whether generation of the `InResponseTo` element should be skipped for subject confirmations. Default is `false`.
| `skipGeneratingSubjectConfirmationNotOnOrAfter` | Whether generation of the `NotOnOrBefore` element should be skipped for subject confirmations. Default is `false`.
| `skipGeneratingSubjectConfirmationRecipient` | Whether generation of the `Recipient` element should be skipped for subject confirmations. Default is `false`.
| `skipGeneratingSubjectConfirmationNotBefore` | Whether generation of the `NotBefore` element should be skipped for subject confirmations. Default is `true`.
| `skipGeneratingSubjectConfirmationNameId` | Whether generation of the `NameID` element should be skipped for subject confirmations. Default is `true`.
| `signingCredentialFingerprint` | `SHA-1` digest of the signing credential's public key, parsed as a regular expression, used for the purposes of key rotation when dealing with multiple credentials.
| `signingCredentialType` | Acceptable values are `BASIC` and `X509`. This setting controls the type of the signature block produced in the final SAML response for this application. The latter, being the default, encodes the signature in `PEM` format inside a `X509Data` block while the former encodes the signature based on the resolved public key under a `DEREncodedKeyValue` block.
| `signingSignatureReferenceDigestMethods` | Collection of signing signature reference digest methods, if any, to override the global defaults.
| `signingKeyAlgorithm` | Signing key algorithm to forcibly use for signing operations when loading the private key. Default is `RSA`.
| `signingSignatureAlgorithms` | Collection of signing signature algorithms, if any, to override the global defaults.
| `signingSignatureBlackListedAlgorithms` | Collection of rejected signing signature algorithms, if any, to override the global defaults.
| `signingSignatureWhiteListedAlgorithms` | Collection of allowed signing signature algorithms, if any, to override the global defaults.
| `signingSignatureCanonicalizationAlgorithm` | The signing signature canonicalization algorithm, if any, to override the global defaults.
| `encryptionDataAlgorithms` | Collection of encryption data algorithms, if any, to override the global defaults.
| `encryptionKeyAlgorithms` | Collection of encryption key transport algorithms, if any, to override the global defaults.
| `encryptionBlackListedAlgorithms` | Collection of rejected encryption algorithms, if any, to override the global defaults.
| `encryptionWhiteListedAlgorithms` | Collection of allowed encryption algorithms, if any, to override the global defaults.
| `whiteListBlackListPrecedence` | Preference value indicating which should take precedence when both whitelist and blacklist are non-empty. Accepted values are `INCLUDE` or `EXCLUDE`. Default is `INCLUDE`.

<div class="alert alert-info"><strong>Keep What You Need!</strong><p>You are encouraged to only keep and maintain properties and settings needed for a 
particular integration. It is UNNECESSARY to grab a copy of all service fields and try to configure them yet again based on their default. While 
you may wish to keep a copy as a reference, this strategy would ultimately lead to poor upgrades increasing chances of breaking changes and a messy 
deployment at that.</p></div>

## Service Provider Integrations

A number of SAML2 service provider integrations are provided natively by CAS. To learn more,
please [review this guide](../integration/Configuring-SAML-SP-Integrations.html).

## Service Provider Metadata

If the SP you wish to integrate with does not produce SAML metadata, you may be able to
use [this service](https://www.samltool.com/sp_metadata.php) to create the metadata,
save it in an XML file and then reference and register it with CAS for the SP.

Alternatively, you may take advantage of a standalone `saml-sp-metadata.json` file that may be found in the same directory
as the CAS metadata artifacts. The contents of this file may be as follows:

```json
{
  "https://example.org/saml": {
    "entityId": "https://example.org/saml",
    "certificate": "MIIDUj...",
    "assertionConsumerServiceUrl": "https://example.org/sso/"
  }
}
```

Each entry in the file is identified by the service provider entity id, allowing CAS to dynamically locate and build the required metadata on the fly
to resume the authentication flow. This may prove easier for those service providers that only present a URL and a signing certificate for the
integration relieving you from creating and managing XML metadata files separately.

The service providers are registered with the CAS service registry as such:

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "https://example.org/saml",
  "name" : "SAMLService",
  "id" : 10000003,
  "metadataLocation" : "json://"
}
```

<div class="alert alert-info"><strong>Metadata Location</strong><p>The metadata location 
in the registration record above needs to be specified as <code>json://</code> to signal 
to CAS that SAML metadata for registered service provider must be fetched from the designated JSON file.</p></div>
