{% include {{ version }}/ldap-configuration.md configKey="cas.authn.attribute-repository.ldap[0]" %}

```properties
# cas.authn.attribute-repository.ldap[0].id=
# cas.authn.attribute-repository.ldap[0].order=0

# cas.authn.attribute-repository.ldap[0].attributes.uid=uid
# cas.authn.attribute-repository.ldap[0].attributes.display-name=displayName
# cas.authn.attribute-repository.ldap[0].attributes.cn=commonName
# cas.authn.attribute-repository.ldap[0].attributes.affiliation=groupMembership
```

To fetch and resolve attributes that carry tags/options, consider tagging the mapped attribute as such:

```properties
# cas.authn.attribute-repository.ldap[0].attributes.affiliation=affiliation;
```
