---
layout: default
title: CAS - SAML2 Metadata Management
category: Protocols
---

{% include variables.html %}

# Git - SAML2 Metadata Management

Metadata documents may also be stored in and fetched from Git repositories. This may specially be used to avoid copying metadata
files across CAS nodes in a cluster, particularly where one needs to deal with more than a few bilateral SAML integrations.
Metadata documents are stored as XML files, and their signing certificate, optionally, is expected to be found in a `.pem`
file by the same name in the repository. (i.e. `SP.xml`'s certificate can be found in `SP.pem`).

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-saml-idp-metadata-git" %}

SAML service definitions must then be designed as follows to allow CAS to fetch metadata documents from Git repositories:

```json
{
  "@class" : "org.apereo.cas.support.saml.services.SamlRegisteredService",
  "serviceId" : "the-entity-id-of-the-sp",
  "name" : "SAMLService",
  "id" : 10000003,
  "description" : "A Git-based metadata resolver",
  "metadataLocation" : "git://"
}
```

Give the above definition, the expectation is that the git repository
contains a `SAMLService.xml` file which may optionally also be accompanied by a `SAMLService.pem` file.

<div class="alert alert-info"><strong>Metadata Location</strong><p>
The metadata location in the registration record above needs to be specified as <code>git://</code> to signal to CAS that 
SAML metadata for registered service provider must be fetched from Git repositories defined in CAS configuration. 
</p></div>

{% include_cached casproperties.html properties="cas.authn.saml-idp.metadata.git" %}

## Identity Provider Metadata

Metadata artifacts that belong to CAS as a SAML2 identity provider may also be
managed and stored via Git. Artifacts such as the metadata, signing and encryption
keys, etc are kept on the file-system in distinct directory locations inside
the repository and data is pushed to or pulled from git repositories on demand.

## Per Service

Identity provider metadata, certificates and keys can also be defined on a per-service basis to override the global defaults.
Metadata documents that would be applicable to a service definition need to adjust the `appliesTo` field in the metadata
document, which is used to construct the directory path to metadata artifacts.
