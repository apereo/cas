      
{% include casproperties.html properties="cas.authn.x509.principal,cas.authn.x509.principal-type" %}

X.509 principal resolution can act on the following principal types:

| Type                    | Description
|-------------------------|----------------------------------------------------------------------
| `SERIAL_NO`             | Resolve the principal by the serial number with a configurable <strong>radix</strong>, ranging from 2 to 36. If <code>radix</code> is <code>16</code>, then the serial number could be filled with leading zeros to even the number of digits.
| `SERIAL_NO_DN`          | Resolve the principal by serial number and issuer dn.
| `SUBJECT`               | Resolve the principal by extracting one or more attribute values from the certificate subject DN and combining them with intervening delimiters.
| `SUBJECT_ALT_NAME`      | Resolve the principal by the subject alternative name extension. (type: otherName)
| `SUBJECT_DN`            | The default type; Resolve the principal by the certificate's subject dn.
| `CN_EDIPI`              | Resolve the principal by the Electronic Data Interchange Personal Identifier (EDIPI) from the Common Name.
| `RFC822_EMAIL`          | Resolve the principal by the [RFC822 Name](https://tools.ietf.org/html/rfc5280#section-4.2.1.6) (aka E-mail address) type of subject alternative name field.

For the `CN_EDIPI`,`SUBJECT_ALT_NAME`, and `RFC822_EMAIL` principal resolvers, since not all certificates have those attributes,
you may specify the following property in order to have a different attribute from the certificate used as the principal.  
If no alternative attribute is specified then the principal will be null and CAS will fail auth or use a different authenticator.

{% include casproperties.html properties="cas.authn.x509.alternate-principal-attribute" %}

### `SUBJECT_DN` Principal Resolution

{% include {{ version }}/subject-dnformat-x509-configuration.md %}

### `CN_EDIPI` Principal Resolution

{% include casproperties.html properties="cas.authn.x509.cn-edipi" %}

### `RFC822_EMAIL` Principal Resolution

{% include casproperties.html properties="cas.authn.x509.rfc822-email" %}

### `SERIAL_NO` Principal Resolution

{% include casproperties.html properties="cas.authn.x509.serial-no" %}

### `SERIAL_NO_DN` Principal Resolution

{% include casproperties.html properties="cas.authn.x509.serial-no-dn" %}

### `SUBJECT_ALT_NAME` Principal Resolution

{% include casproperties.html properties="cas.authn.x509.subject-alt-name" %}
