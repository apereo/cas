---
layout: default
title: CAS - FIDO2 WebAuthn Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# FIDO2 WebAuthn Multifactor Authentication - Attestation Trust & Metadata

WebAuthn support in CAS can handle authenticator attestation, which provides a way for the 
web service to request cryptographic proof of what authenticator the user is using.

Attestation trust metadata can be taught to CAS using the following strategies.

{% tabs fidotrustmd %}

{% tab fidotrustmd <i class="fa fa-code px-1"></i> JSON %}
           
WebAuthn attestation trust metadata can be loaded from a JSON file and CAS by default ships with 
and has enabled metadata offered by Yubico, for a series of devices that are also primarily offered by Yubico. 

The structure of this JSON file is as follows:

```json
{
  "identifier": "...",
  "version": 1,
  "vendorInfo": {
    "url": "...",
    "imageUrl": "...",
    "name": "..."
  },
  "trustedCertificates": [ "..." ],
  "devices": [
    {
      "deviceId": "...",
      "displayName": "...",
      "transports": 4,
      "deviceUrl": "...",
      "imageUrl": ".",
      "selectors": [
        {
          "type": "x509Extension",
          "parameters": {
            "key": "...",
            "value": {
              "type": "hex",
              "value": "..."
            }
          }
        }
      ]
    }
  ]
}
```

{% include_cached casproperties.html properties="cas.authn.mfa.web-authn.core.trust-source" includes=".trusted-device-metadata" %}

{% endtab %}

{% tab fidotrustmd FIDO %}

The [FIDO Alliance Metadata Service (MDS)](https://fidoalliance.org/metadata/) is a centralized repository of the Metadata Statement that is used by the 
relying parties to validate authenticator attestation and prove the genuineness of the device model. MDS also provides 
information about certification status of the authenticators, and found security issues. Organizations deploying FIDO 
Authentication are able to use this information to select specific certification levels as required for compliance, and 
work through the security notifications to ensure effective incident response.

<div class="alert alert-info">:information_source: <strong>Usage</strong><p>
To activate this functionality, you will need to accept the legal terms and conditions that are put together by FIDO Alliance
via CAS configuration by specifying the appropriate legal header.</p></div>

{% include_cached casproperties.html properties="cas.authn.mfa.web-authn.core.trust-source" includes=".fido" %}

{% endtab %}

{% endtabs %}
