# Static Resource Graphical User Authentication

Source: https://apereo.github.io/cas/development/authentication/GUA-Authentication-Storage-Resource.html

Primarily useful for demo and testing purposes, this option allows CAS to load a global and static image resource as the user identifier onto the login flow.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.gua.simple` (Optional; type `Map<String,String>`): Locate GUA settings and images from a static image per user. This is treated as a `Map` where the key is the user id and the value should be the graphical resource.
