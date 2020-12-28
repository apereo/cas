A JDBC querying handler that will pull back the password and the private salt value for a user and validate the encoded
password using the public salt value. Assumes everything is inside the same database table. Supports settings for
number of iterations as well as private salt.

This password encoding method combines the private Salt and the public salt which it prepends to the password before hashing.
If multiple iterations are used, the bytecode hash of the first iteration is rehashed without the salt values. The final hash
is converted to hex before comparing it to the database value.

```properties
# cas.authn.jdbc.encode[0].number-of-iterations=0
# cas.authn.jdbc.encode[0].number-of-iterations-field-name=numIterations
# cas.authn.jdbc.encode[0].salt-field-name=salt
# cas.authn.jdbc.encode[0].static-salt=
# cas.authn.jdbc.encode[0].sql=
# cas.authn.jdbc.encode[0].algorithm-name=
# cas.authn.jdbc.encode[0].password-field-name=password
# cas.authn.jdbc.encode[0].expired-field-name=
# cas.authn.jdbc.encode[0].disabled-field-name=

# cas.authn.jdbc.encode[0].credential-criteria=
# cas.authn.jdbc.encode[0].name=
# cas.authn.jdbc.encode[0].order=0
```
