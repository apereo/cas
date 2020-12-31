{% include {{ version }}/rdbms-configuration.md configKey="cas.authn.attribute-repository.jdbc[0]" %}

To fetch and resolve attributes from SQL databases, consider the following options:

```properties
# cas.authn.attribute-repository.jdbc[0].attributes.uid=uid
# cas.authn.attribute-repository.jdbc[0].attributes.display-name=displayName
# cas.authn.attribute-repository.jdbc[0].attributes.cn=commonName
# cas.authn.attribute-repository.jdbc[0].attributes.affiliation=groupMembership

# cas.authn.attribute-repository.jdbc[0].single-row=true
# cas.authn.attribute-repository.jdbc[0].order=0
# cas.authn.attribute-repository.jdbc[0].id=
# cas.authn.attribute-repository.jdbc[0].require-all-attributes=true
# cas.authn.attribute-repository.jdbc[0].case-canonicalization=NONE|LOWER|UPPER
# cas.authn.attribute-repository.jdbc[0].query-type=OR|AND
# cas.authn.attribute-repository.jdbc[0].case-insensitive-query-attributes=username

# Used only when there is a mapping of many rows to one user
# cas.authn.attribute-repository.jdbc[0].column-mappings.column-attr-name1=columnAttrValue1
# cas.authn.attribute-repository.jdbc[0].column-mappings.column-attr-name2=columnAttrValue2
# cas.authn.attribute-repository.jdbc[0].column-mappings.column-attr-name3=columnAttrValue3

# cas.authn.attribute-repository.jdbc[0].sql=SELECT * FROM table WHERE {0}
# cas.authn.attribute-repository.jdbc[0].username=uid
```
