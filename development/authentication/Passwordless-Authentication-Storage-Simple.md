# Simple Passwordless Authentication Storage

Source: https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Storage-Simple.html

This strategy provides a static map of usernames that are linked to their method of contact, such as email or phone number. It is best used for testing and demo purposes. The key in the map is taken to be the username eligible for authentication while the value can either be an email address or phone number that would be used to contact the user with issued tokens.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.passwordless.accounts.simple` (Optional; type `Map<String,String>`): Passwordless authentication settings using static accounts. The key is the user identifier, while the value is the form of contact such as email, sms, etc.
