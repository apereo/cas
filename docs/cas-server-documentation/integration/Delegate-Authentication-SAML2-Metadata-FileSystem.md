---
layout: default
title: CAS - SAML2 Delegated Authentication
category: Authentication
---

{% include variables.html %}

# SAML2 Delegated Authentication - FileSystem Service Provider Metadata

SAML2 metadata for CAS as the SAML2 service provider is typically managed on disk, and generated on startup if the metadata file
is not found. Future and subsequent changes to this metadata file, if necessary, must be handled manually and the file might
need to be curated and edited to fit your purposes. This is the default option.

{% include_cached casproperties.html properties="cas.authn.pac4j.saml[].metadata.service-provider"  includes=".file-system" %}
