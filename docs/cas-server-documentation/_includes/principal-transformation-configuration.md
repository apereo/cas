<!-- fragment:keep -->

#### Principal Transformation

Authentication handlers that generally deal with username-password credentials
can be configured to transform the user id prior to executing the authentication sequence.
The following options may be used:

| Type                    | Description
|-------------------------|----------------------------------------------------------
| `NONE`                  | Do not apply any transformations.
| `UPPERCASE`             | Convert the username to uppercase.
| `LOWERCASE`             | Convert the username to lowercase.

Authentication handlers as part of principal transformation may also be provided a path to a 
Groovy script to transform the provided username. The outline of the script may take on the following form:

```groovy
def String run(final Object... args) {
    def providedUsername = args[0]
    def logger = args[1]
    return providedUsername.concat("SomethingElse")
}
```

