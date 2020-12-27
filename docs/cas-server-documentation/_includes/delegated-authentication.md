### Delegated Authentication Configuration

The following options are shared and apply when CAS is configured to delegate authentication
to an external provider such as Twitter, GitHub, etc:

```properties
# {{ include.configKey }}.id=
# {{ include.configKey }}.secret=
# {{ include.configKey }}.client-name=My Provider
# {{ include.configKey }}.auto-redirect=false
# {{ include.configKey }}.css-class=
# {{ include.configKey }}.callback-url=
# {{ include.configKey }}.principal-attribute-id=
# {{ include.configKey }}.enabled=true
# {{ include.configKey }}.callback-url-type=PATH_PARAMETER|QUERY_PARAMETER|NONE
```

The following types are supported with callback URL resolution:

| Type               | Description
|--------------------|--------------------------------------------------------------------------------------
| `PATH_PARAMETER`   | When constructing a callback URL, client name is added to the url as a path parameter.
| `QUERY_PARAMETER`  | When constructing a callback URL, client name is added to the url as a query parameter.
| `NONE`             | No client name is added to the url.

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
