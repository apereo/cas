```properties
# Activate MFA globally based on principal attributes
# cas.authn.mfa.global-principal-attribute-name-triggers=memberOf,eduPersonPrimaryAffiliation

# Specify the regular expression pattern to trigger multifactor when working with a single provider.
# Comment out the setting when working with multiple multifactor providers
# cas.authn.mfa.global-principal-attribute-value-regex=faculty|staff

# Activate MFA globally based on principal attributes and a groovy-based predicate
# cas.authn.mfa.global-principal-attribute-predicate=file:/etc/cas/PredicateExample.groovy
```
