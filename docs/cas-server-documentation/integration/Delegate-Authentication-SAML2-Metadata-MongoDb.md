---
layout: default
title: CAS - SAML2 Delegated Authentication
category: Authentication
---

{% include variables.html %}

# SAML2 Delegated Authentication - MongoDb Service Provider Metadata

SAML2 metadata for CAS as the SAML2 service provider may also be managed inside a MongoDb instance. To active this feature, you need to start by including 
the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-mongo-core" %}

{% include_cached featuretoggles.html features="DelegatedAuthentication.saml-mongodb" %}
                                                                                    
Finally, please make sure you have specified a collection name in your CAS settings that would ultimately house the generated SAML2 metadata.

{% include_cached casproperties.html properties="cas.authn.pac4j.saml[].metadata.service-provider" includes="mongo" %}
