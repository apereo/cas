Define whether CAS should include and release protocol attributes defined in the specification in addition to the
principal attributes. By default all authentication attributes are released when protocol attributes are enabled for
release. If you wish to restrict which authentication 
attributes get released, you can use the below settings to control authentication attributes more globally.

```properties
# cas.authn.authentication-attribute-release.only-release=authenticationDate,isFromNewLogin
# cas.authn.authentication-attribute-release.never-release=
# cas.authn.authentication-attribute-release.enabled=true
```

Protocol/authentication attributes may also be released conditionally on a per-service
basis. 
