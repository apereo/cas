### Person Directory Principal Resolution

The following options related to Person Directory support in CAS when it attempts to
resolve and build the authenticated principal:

```properties
# {{ include.configKey }}.principal-attribute=uid,sAMAccountName,etc
# {{ include.configKey }}.return-null=false
# {{ include.configKey }}.principal-resolution-failure-fatal=false
# {{ include.configKey }}.use-existing-principal-id=false
# {{ include.configKey }}.attribute-resolution-enabled=true
# {{ include.configKey }}.active-attribute-repository-ids=StubRepository,etc
```

