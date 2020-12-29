Authenticates a user by comparing the user password (which can be encoded with a password encoder)
against the password on record determined by a configurable database query.

```properties
# cas.authn.jdbc.query[0].credential-criteria=
# cas.authn.jdbc.query[0].name=
# cas.authn.jdbc.query[0].order=0

# cas.authn.jdbc.query[0].sql=SELECT * FROM table WHERE name=?
# cas.authn.jdbc.query[0].field-password=password
# cas.authn.jdbc.query[0].field-expired=
# cas.authn.jdbc.query[0].field-disabled=
# cas.authn.jdbc.query[0].principal-attribute-list=sn,cn:commonName,givenName
```

{% include {{ version }}/authentication-credential-selection-configuration.md %}
