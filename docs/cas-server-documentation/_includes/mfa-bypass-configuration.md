### Multifactor Authentication Bypass Configuration

The following bypass options apply equally to multifactor authentication providers:

```properties
# {{ include.configKey }}.bypass.principal-attribute-name=bypass|skip
# {{ include.configKey }}.bypass.principal-attribute-value=true|enabled.+

# {{ include.configKey }}.bypass.authentication-attribute-name=bypass|skip
# {{ include.configKey }}.bypass.authentication-attribute-value=allowed.+|enabled.+

# {{ include.configKey }}.bypass.authentication-handler-name=AcceptUsers.+
# {{ include.configKey }}.bypass.authentication-method-name=LdapAuthentication.+

# {{ include.configKey }}.bypass.credential-class-type=UsernamePassword.+

# {{ include.configKey }}.bypass.http-request-remote-address=127.+|example.*
# {{ include.configKey }}.bypass.http-request-headers=header-X-.+|header-Y-.+

# {{ include.configKey }}.bypass.groovy.location=file:/etc/cas/config/mfa-bypass.groovy
```

If multifactor authentication bypass is determined via REST, then the following
settings can be used to configure the REST integration:

{% include {{ version }}/rest-integration.md configKey="{{ include.configKey }}.bypass.rest"  %}

{% include {{ version }}/mfa-configuration.md configKey="{{ include.configKey }}" %}
