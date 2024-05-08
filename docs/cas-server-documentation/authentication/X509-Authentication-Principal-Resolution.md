---
layout: default
title: CAS - X.509 Authentication
category: Authentication
---
{% include variables.html %}

# X.509 Authentication - Principal Resolution
                         
X.509 authentication is about rules and conditions that would allow CAS to extract a subject identifier from the client 
certificate presented during the SSL/TLS handshake process and turning into a CAS principal. This is a critical step
in the authentication process to ensure that the principal identifier extracted from the certificate is meaningful and
can be used to locate and identify the user for attributes later on.

{% include_cached casproperties.html properties="cas.authn.x509.principal.,cas.authn.x509.principal-type" %}
           
## Principal Resolution Types

The following principal resolution types are available.

{% tabs x509principalresolution %}

{% tab x509principalresolution SUBJECT_DN %}

{% include_cached casproperties.html properties="cas.authn.x509.subject-dn" %}

{% endtab %}

{% tab x509principalresolution CN_EDIPI %}

{% include_cached casproperties.html properties="cas.authn.x509.cn-edipi" %}

{% endtab %}

{% tab x509principalresolution RFC822_EMAIL %}

{% include_cached casproperties.html properties="cas.authn.x509.rfc822-email" %}

{% endtab %}

{% tab x509principalresolution SERIAL_NO %}

{% include_cached casproperties.html properties="cas.authn.x509.serial-no" %}

{% endtab %}

{% tab x509principalresolution SERIAL_NO_DN %}

{% include_cached casproperties.html properties="cas.authn.x509.serial-no-dn" %}

{% endtab %}

{% tab x509principalresolution SUBJECT_ALT_NAME %}

{% include_cached casproperties.html properties="cas.authn.x509.subject-alt-name" %}

{% endtab %}

{% endtabs %}
