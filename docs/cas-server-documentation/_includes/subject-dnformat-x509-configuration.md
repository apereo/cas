```properties
# cas.authn.x509.subject-dn.format=[DEFAULT,RFC1779,RFC2253,CANONICAL]
```

| Type          | Description
|---------------|----------------------------------------------------------------------
| `DEFAULT`     | Calls certificate.getSubjectDN() method for backwards compatibility but that method is ["denigrated"](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/cert/X509Certificate.html#getIssuerDN()).
| `RFC1779`     | Calls [X500Principal.getName("RFC1779")](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/javax/security/auth/x500/X500Principal.html#getName()) which emits a subject DN with the attribute keywords defined in RFC 1779 (CN, L, ST, O, OU, C, STREET). Any other attribute type is emitted as an OID.
| `RFC2253`     | Calls [X500Principal.getName("RFC2253")](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/javax/security/auth/x500/X500Principal.html#getName()) which emits a subject DN with the attribute keywords defined in RFC 2253 (CN, L, ST, O, OU, C, STREET, DC, UID). Any other attribute type is emitted as an OID.
| `CANONICAL`   | Calls X500Principal.getName("CANONICAL" which emits a subject DN that starts with RFC 2253 and applies additional canonicalizations described in the [javadoc](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/javax/security/auth/x500/X500Principal.html#getName()).
