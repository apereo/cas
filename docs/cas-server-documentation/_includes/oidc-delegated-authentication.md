### OpenID Connect Delegated Authentication Configuration

The following options are shared and apply when CAS is configured to delegate authentication
to an external OpenID Connect provider such as Azure AD or Amazon Cognito, etc:

```properties
# {{ include.configKey }}.discovery-uri=
# {{ include.configKey }}.logout-url=
# {{ include.configKey }}.max-clock-skew=
# {{ include.configKey }}.scope=
# {{ include.configKey }}.use-nonce=false
# {{ include.configKey }}.disable-nonce=false
# {{ include.configKey }}.preferred-jws-algorithm=
# {{ include.configKey }}.response-mode=
# {{ include.configKey }}.response-type=
# {{ include.configKey }}.custom-params.param1=value1
# {{ include.configKey }}.read-timeout=PT5S
# {{ include.configKey }}.connect-timeout=PT5S
# {{ include.configKey }}.expire-session-with-token=false
# {{ include.configKey }}.token-expiration-advance=0
```
