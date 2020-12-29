{% include {{ version }}/rest-integration.md configKey="cas.authn.attribute-repository.rest[0]" %}

```properties
# cas.authn.attribute-repository.rest[0].order=0
# cas.authn.attribute-repository.rest[0].id=
# cas.authn.attribute-repository.rest[0].case-insensitive=false
```

The authenticating user id is passed in form of a request parameter under `username`. The response is expected
to be a JSON map as such:

```json
{
  "name" : "JohnSmith",
  "age" : 29,
  "messages": ["msg 1", "msg 2", "msg 3"]
}
```
