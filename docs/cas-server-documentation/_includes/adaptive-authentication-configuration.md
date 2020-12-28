Control how CAS authentication should adapt itself to incoming client requests.

```properties
# cas.authn.adaptive.reject-countries=United.+
# cas.authn.adaptive.reject-browsers=Gecko.+
# cas.authn.adaptive.reject-ip-addresses=127.+

# cas.authn.adaptive.require-multifactor.mfa-duo=127.+|United.+|Gecko.+
```

Adaptive authentication can also react to specific times in order to trigger multifactor authentication.

```properties
# cas.authn.adaptive.require-timed-multifactor[0].provider-id=mfa-duo
# cas.authn.adaptive.require-timed-multifactor[0].on-or-after-hour=20
# cas.authn.adaptive.require-timed-multifactor[0].on-or-before-hour=7
# cas.authn.adaptive.require-timed-multifactor[0].on-days=Saturday,Sunday
```
