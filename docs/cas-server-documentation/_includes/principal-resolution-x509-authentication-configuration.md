```properties
# cas.authn.x509.principal-type=SERIAL_NO|SERIAL_NO_DN|SUBJECT|SUBJECT_ALT_NAME|SUBJECT_DN
```

{% include {{ version }}/persondirectory-configuration.md configKey="cas.authn.x509.principal" %}

{% include {{ version }}/principal-transformation-configuration.md configKey="cas.authn.x509" %}

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

```properties
# cas.authn.x509.alternate-principal-attribute=subjectDn|sigAlgOid|subjectX500Principal|x509Rfc822Email|x509subjectUPN
```

### `SUBJECT_DN` Principal Resolution

{% include {{ version }}/subject-dnformat-x509-configuration.md %}

### `CN_EDIPI` Principal Resolution

{% include {{ version }}/edipi-principal-resolution-x509-authentication-configuration.md %}

### `RFC822_EMAIL` Principal Resolution

{% include {{ version }}/rfc822-principal-resolution-x509-authentication-configuration.md %}

### `SERIAL_NO` Principal Resolution

{% include {{ version }}/serialno-principal-resolution-x509-authentication-configuration.md %}

### `SERIAL_NO_DN` Principal Resolution

{% include {{ version }}/serialno-dn-principal-resolution-x509-authentication-configuration.md %}

### `SUBJECT_ALT_NAME` Principal Resolution

{% include {{ version }}/subjectaltname-principal-resolution-x509-authentication-configuration.md %}
