{% include {{ version }}/couchdb-integration.md configKey="cas.authn.surrogate" %}.

Surrogates may be stored either as part of the principals profile or as a
series of principal/surrogate pair. The default is a key/value pair.

```properties
# cas.authn.surrogate.ldap.surrogate-search-filter=(&(principal={user})(memberOf=cn=edu:example:cas:something:{user},dc=example,dc=edu))
# cas.authn.surrogate.ldap.member-attribute-name=memberOf
# cas.authn.surrogate.ldap.member-attribute-value-regex=cn=edu:example:cas:something:([^,]+),.+
```
