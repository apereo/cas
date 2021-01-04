{% include {{ version }}/ldap-configuration.md configKey="cas.authn.surrogate.ldap" %}

```properties
# cas.authn.surrogate.ldap.surrogate-search-filter=(&(principal={user})(memberOf=cn=edu:example:cas:something:{user},dc=example,dc=edu))
# cas.authn.surrogate.ldap.member-attribute-name=memberOf
# cas.authn.surrogate.ldap.member-attribute-value-regex=cn=edu:example:cas:something:([^,]+),.+
```
