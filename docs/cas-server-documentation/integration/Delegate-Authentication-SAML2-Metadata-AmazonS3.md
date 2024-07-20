---
layout: default
title: CAS - SAML2 Delegated Authentication
category: Authentication
---

{% include variables.html %}

# SAML2 Delegated Authentication - Amazon S3 Service Provider Metadata

SAML2 metadata for CAS as the SAML2 service provider may also be managed inside an Amazon S3 bucket. A single bucket is created by default automatically
that is able to store different objects for each service provider and metadata entity.

To activate this feature, you need to start by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-aws" %}

{% include_cached featuretoggles.html features="DelegatedAuthentication.saml-s3" %}

{% include_cached casproperties.html properties="cas.authn.pac4j.saml[].metadata.service-provider" includes="amazon-s3" %}
