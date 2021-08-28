---
layout: default
title: CAS - ACME Integration
category: Integration
---

{% include variables.html %}

# Overview

Enable support for the *Automatic Certificate Management Environment* (ACME) protocol 
as specified in [RFC 8555](https://tools.ietf.org/html/rfc8555). ACME is a protocol that a 
certificate authority (CA) such as Let's Encrypt and an applicant can use to automate the process 
of verification and certificate issuance.

Support is enabled by including the following dependency in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-acme" %}

The activation of this module is performed on startup when the CAS server is ready serve http requests. 
When this module is finished successfully, the domain key pair composed of the key and 
the certificate (including the full certificate path) will be produced in separate files.

If no account was registered with the CA yet, there will also be a new key as the account key pair.

The CSR file contains the CSR that was used for the certificate order. It is generated for convenience 
and will not be needed later again. When the certificate is renewed, a new CSR will be generated.

There are two sets of key pairs. One is required for creating and accessing 
your account and the other is required for encrypting the traffic on your 
domain(s). It is strongly encouraged to use separate key pairs 
for account and each of the certificates.

<div class="alert alert-info">
<strong>Note</strong><br/>
Backup key pairs in a safe place, as you will be locked out from your account 
if you should ever lose it! There is no way to recover a lost key pair, 
or regain access to your account when the key is lost.
</div>

## HTTP Challenge

For the HTTP challenge, CAS server will respond to `GET` requests at
`/.well-known/acme-challenge/{token}` path. The request is
always performed against port `80` and at the root of the domain which 
means CAS MUST be able to operate at that port and context 
path to respond to verification challenges.

## Configuration

{% include_cached casproperties.html properties="cas.acme." %}

