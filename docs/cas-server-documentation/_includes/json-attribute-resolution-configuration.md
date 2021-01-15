If you wish to directly and separately retrieve attributes from a static JSON source,
the following settings are then relevant:

{% include casproperties.html properties="cas.authn.attribute-repository.json" %}

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
