---
layout: default
title: CAS - Password Policy Enforcement
---

# Password Policy Enforcement

The purpose of the is twofold:

- Detect a number of scenarios that would otherwise prevent user authentication based on user account status.
- Warn users whose account status is near a configurable expiration date and redirect the flow to an external 
identity management system.

<div class="alert alert-danger"><strong>No Password Management!</strong><p>LPPE is not about password management.
If you are looking for that sort of capability, you might be interested in
<a href="http://code.google.com/p/pwm/">http://code.google.com/p/pwm/</a></p></div>

## LDAP

The below scenarios are by default considered errors preventing authentication in a generic manner through
 the normal CAS login flow. LPPE intercepts the authentication flow, detecting the above standard error codes. 
 Error codes are then translated into proper messages in the CAS login flow and would allow the user to take proper action, 
 fully explaining the nature of the problem.

- `ACCOUNT_LOCKED`
- `ACCOUNT_DISABLED`
- `INVALID_LOGON_HOURS`
- `INVALID_WORKSTATION`
- `PASSWORD_MUST_CHANGE`
- `PASSWORD_EXPIRED`

The translation of LDAP errors into CAS workflow is all 
handled by [ldaptive](http://www.ldaptive.org/docs/guide/authentication/accountstate).

### Account Expiration Notification
LPPE is also able to warn the user when the account is about to expire. The expiration policy is 
determined through pre-configured Ldap attributes with default values in place.



### Configuration

```xml
<alias name="ldapPasswordPolicyConfiguration" alias="passwordPolicyConfiguration" />
```

The following settings are applicable:

```properties
# password.policy.warnAll=false
# password.policy.warningDays=30
# password.policy.url=https://password.example.edu/change
```

Next, in your `ldapAuthenticationHandler` bean, configure the password policy configuration above:

```xml
<bean id="ldapAuthenticationHandler"
      class="org.apereo.cas.authentication.LdapAuthenticationHandler"
      p:passwordPolicyConfiguration-ref="passwordPolicyConfiguration">
      ...
</bean>
```  

Next, make sure `Authenticator` is set to enable/use password policy:

```xml
<ldaptive:bind-search-authenticator id="authenticator">
    ...
    <ldaptive:authentication-response-handler>
      <ldaptive:password-policy-handler />
    </ldaptive:authentication-response-handler>
</ldaptive:bind-search-authenticator>
{% endhighlight %}

Other available password policy handlers that could be associated with an `authenticator` are:

{% highlight xml %}
...
<ldaptive:e-directory-handler warningPeriod="..." />
...
<ldaptive:free-ipa-handler expirationPeriod="..." maxLoginFailures="..." warningPeriod="..." />
...
<ldaptive:password-expiration-handler />
...
```

            
### Components

#### Default

The default account state handler, that calculates the password expiration warning period,
maps LDAP errors to the CAS workflow.

#### Optional

Supports both opt-in and opt-out warnings on a per-user basis.

```xml
<alias name="optionalWarningAccountStateHandler" alias="passwordPolicyConfiguration" />
```

```properties
# password.policy.warn.attribute.name=attributeName
# password.policy.warn.attribute.value=attributeValue
# password.policy.warn.display.matched=true
```
