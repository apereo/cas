{% include casproperties.html properties="cas.authn.attribute-repository.ldap" %}

To fetch and resolve attributes that carry tags/options, consider tagging the mapped attribute as such:

```properties
# cas.authn.attribute-repository.ldap[0].attributes.affiliation=affiliation;
```
