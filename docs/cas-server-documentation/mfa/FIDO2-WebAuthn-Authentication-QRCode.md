---
layout: default
title: CAS - FIDO2 WebAuthn Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# FIDO2 WebAuthn Multifactor Authentication - QR Codes
         
CAS can be configured to support FIDO2 WebAuthn authentication using QR codes. Once enabled, this 
feature allows users to authenticate using a FIDO2-enabled device by scanning a QR code 
presented by CAS. This authentication flow is mainly useful in scenarios where the primary
authentication device (e.g. a laptop) is separate from the device that is used for FIDO2
authentication (e.g. a mobile phone). The flow follows these steps:

- The user is prompted for their FIDO2-enabled device on their primary authentication device (i.e. laptop).
  - User decides here that FIDO2 authentication on the primary device is impossible or impractical.
- CAS in parallel presents a QR code on the primary device that is then scanned by the user's secondary FIDO2-enabled device (i.e. phone).
- The scanned QR code contains a link that prompts the user to complete the FIDO authentication attempt with CAS.
  - This attempt is handled directly on the user's secondary device, completely separate from the primary device.
- Once the user has established a session on the secondary device, the primary authentication device is notified and will resume the flow.

{% include_cached casproperties.html properties="cas.authn.mfa.web-authn.core" %}
