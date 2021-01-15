Authenticates a user by comparing the user password (which can be encoded with a password encoder)
against the password on record determined by a configurable database query.

{% include casproperties.html properties="cas.authn.jdbc.query" %}

{% include {{ version }}/authentication-credential-selection-configuration.md %}
