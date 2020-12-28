```properties
# cas.acceptable-usage-policy.in-memory.scope=GLOBAL|AUTHENTICATION
```                                                    

The following scopes are supported:

| Scope                | Description
|----------------------|----------------------------------
| `GLOBAL`             | Store decisions in the global in-memory map (for life of server).
| `AUTHENTICATION`     | Store decisions such that user is prompted when they authenticate via credentials.
