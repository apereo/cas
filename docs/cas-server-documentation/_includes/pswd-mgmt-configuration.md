Allow the user to update their account password, etc in-place.

```properties
# cas.authn.pm.enabled=true
# cas.authn.pm.captcha-enabled=false

# Minimum 8 and Maximum 10 characters at least 1 Uppercase Alphabet, 1 Lowercase Alphabet, 1 Number and 1 Special Character
# cas.authn.pm.policy-pattern=^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,10}

# cas.authn.pm.reset.expirationMinutes=1
# cas.authn.pm.reset.security-questions-enabled=true

# Whether the Password Management Token will contain the client or server IP Address.
# cas.authn.pm.reset.include-server-ip-address=true
# cas.authn.pm.reset.include-client-ip-address=true

# Automatically log in after successful password change
# cas.authn.pm.auto-login=false
```
