---
layout: default
title: CAS - SAML2 Delegated Authentication
category: Authentication
---

{% include variables.html %}

#  SAML2 Delegated Authentication - Metadata Aggregates

CAS allows you to aggregate multiple SAML2 identity provider metadata documents into a single
metadata document. Using this, you may design the delegation authentication workflow with CAS acting
as a single service provider supporting many identity providers all in the same single configuration block,
instead of having to configure and register each identity provider separately.

The metadata aggregate document tends to have the following structure:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<EntitiesDescriptor xmlns="urn:oasis:names:tc:SAML:2.0:metadata"
                    xmlns:alg="urn:oasis:names:tc:SAML:metadata:algsupport"
                    xmlns:ds="http://www.w3.org/2000/09/xmldsig#"
                    xmlns:icmd="http://id.incommon.org/metadata"
                    xmlns:idpdisc="urn:oasis:names:tc:SAML:profiles:SSO:idp-discovery-protocol"
                    xmlns:init="urn:oasis:names:tc:SAML:profiles:SSO:request-init"
                    xmlns:mdattr="urn:oasis:names:tc:SAML:metadata:attribute"
                    xmlns:mdrpi="urn:oasis:names:tc:SAML:metadata:rpi"
                    xmlns:mdui="urn:oasis:names:tc:SAML:metadata:ui"
                    xmlns:remd="http://refeds.org/metadata"
                    xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                    xmlns:shibmd="urn:mace:shibboleth:metadata:1.0"
                    xmlns:xenc="http://www.w3.org/2001/04/xmlenc#"
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    ID="ABC20250317T191849" Name="urn:mace:example">
                    
    <EntityDescriptor entityID="urn:mace:incommon:example.edu">
        <IDPSSODescriptor>
            ...
        <IDPSSODescriptor/>
    </EntityDescriptor>
    
    <EntityDescriptor entityID="urn:mace:incommon:system.org">
        <IDPSSODescriptor>
            ...
        <IDPSSODescriptor/>
    </EntityDescriptor>

</EntitiesDescriptor>
```
  
When processed, each individual `EntityDescriptor` element is extracted and treated as a separate identity provider
using a simple randomized strategy to identify and name each identity provider as a `SAML2Client` in CAS. Where possible
and metadata permitting, `MDUI` information is extracted from the metadata aggregate and used to populate the identity provider's
display name, description and logo.

<div class="alert alert-info">:information_source: <strong>Note</strong><p>If the metadata aggregate is super large
and includes thousands of identity provider definitions, you MUST ensure your CAS deployment runs with enough
memory to be able to load, parse and process the XML data and registering identity providers.</p></div>

