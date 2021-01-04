If you wish to directly and separately retrieve attributes from a static JSON source,
the following settings are then relevant:

```properties
# cas.authn.attribute-repository.json[0].location=file://etc/cas/attribute-repository.json
# cas.authn.attribute-repository.json[0].order=0
# cas.authn.attribute-repository.json[0].id=
```

The format of the file may be:

```json
{
    "user1": {
        "firstName":["Json1"],
        "lastName":["One"]
    },
    "user2": {
        "firstName":["Json2"],
        "eduPersonAffiliation":["employee", "student"]
    }
}
```
