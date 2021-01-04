```properties
# cas.authn.mfa.acceptto.application-id=
# cas.authn.mfa.acceptto.secret=
# cas.authn.mfa.acceptto.organization-id=
# cas.authn.mfa.acceptto.organization-secret=

# cas.authn.mfa.acceptto.authn-selection-url=https://mfa.acceptto.com/mfa/index
# cas.authn.mfa.acceptto.api-url=https://mfa.acceptto.com/api/v9/
# cas.authn.mfa.acceptto.message=Do you want to login via CAS?
# cas.authn.mfa.acceptto.timeout=120
# cas.authn.mfa.acceptto.email-attribute=mail    
# cas.authn.mfa.acceptto.group-attribute=    

# cas.authn.mfa.acceptto.registration-api-url=https://mfa.acceptto.com/api/integration/v1/mfa/authenticate
# cas.authn.mfa.acceptto.registration-api-public-key=file:/path/to/publickey.pem

# cas.authn.mfa.acceptto.name=
# cas.authn.mfa.acceptto.order=
# cas.authn.mfa.acceptto.rank=0
```

{% include {{ version }}/mfa-bypass-configuration.md configKey="cas.authn.mfa.acceptto" %}
