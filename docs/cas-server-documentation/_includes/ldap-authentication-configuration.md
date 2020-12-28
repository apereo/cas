CAS authenticates a username/password against an LDAP directory such as Active Directory or OpenLDAP.
There are numerous directory architectures and we provide configuration for four common cases.

Note that CAS will automatically create the appropriate components internally
based on the settings specified below. If you wish to authenticate against more than one LDAP
server, increment the index and specify the settings for the next LDAP server.

**Note:** Attributes retrieved as part of LDAP authentication are merged with all attributes
retrieved from other attribute repository sources, if any.
Attributes retrieved directly as part of LDAP authentication trump all other attributes.

{% include {{ version }}/ldap-configuration.md configKey="cas.authn.ldap[0]" %}

```properties
# Define attributes to be retrieved from LDAP as part of the same authentication transaction
# The left-hand size notes the source while the right-hand size indicate an optional renaming/remapping
# of the attribute definition. The same attribute name is allowed to be mapped multiple times to
# different attribute names.

# cas.authn.ldap[0].principal-attribute-list=sn,cn:commonName,givenName,eduPersonTargettedId:SOME_IDENTIFIER

# cas.authn.ldap[0].collect-dn-attribute=false
# cas.authn.ldap[0].principal-dn-attribute-name=principalLdapDn
# cas.authn.ldap[0].allow-multiple-principal-attribute-values=true
# cas.authn.ldap[0].allow-missing-principal-attribute-value=true
# cas.authn.ldap[0].credential-criteria=
```

To fetch and resolve attributes that carry tags/options, consider tagging the mapped attribute as such:

```properties
# cas.authn.ldap[0].principal-attribute-list=homePostalAddress:homePostalAddress;
```
