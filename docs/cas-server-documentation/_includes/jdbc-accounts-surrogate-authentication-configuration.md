{% include {{ version }}/rdbms-configuration.md configKey="cas.authn.surrogate.jdbc" %}

```properties
# cas.authn.surrogate.jdbc.surrogate-search-query=SELECT COUNT(*) FROM surrogate WHERE username=?
# cas.authn.surrogate.jdbc.surrogate-account-query=SELECT surrogate_user AS surrogateAccount FROM surrogate WHERE username=?
```
