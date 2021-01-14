```properties
# cas.authn.saml-idp.entity-id=https://cas.example.org/idp
# cas.authn.saml-idp.replicate-sessions=false

# cas.authn.saml-idp.authentication-context-class-mappings[0]=urn:oasis:names:tc:SAML:2.0:ac:classes:SomeClassName->mfa-duo
# cas.authn.saml-idp.authentication-context-class-mappings[1]=https://refeds.org/profile/mfa->mfa-gauth

# cas.authn.saml-idp.attribute-friendly-names[0]=urn:oid:1.3.6.1.4.1.5923.1.1.1.6->eduPersonPrincipalName
  
# cas.authn.saml-idp.attribute-query-profile-enabled=true
```
   
{% include casproperties.html properties="cas.client" %}
