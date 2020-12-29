### OAuth20 Delegated Authentication Configuration

The following options are shared and apply when CAS is configured to delegate authentication
to an external OpenID Connect provider such as Azure AD or Amazon Cognito, etc:

```properties
# {{ include.configKey }}.auth-url=
# {{ include.configKey }}.token-url=
# {{ include.configKey }}.profile-url=
# {{ include.configKey }}.profile-path=
# {{ include.configKey }}.scope=
# {{ include.configKey }}.profile-verb=GET|POST
# {{ include.configKey }}.response-type=code
# {{ include.configKey }}.profile-attrs.attr1=path-to-attr-in-profile
# {{ include.configKey }}.custom-params.param1=value1
```
