{% include casproperties.html properties="cas.monitor.endpoints.form-login-enabled" %}

There is a special endpoint named `defaults`  which serves as a
shortcut that controls the security of all endpoints, if left undefined in CAS settings.

Note that any individual endpoint must be first enabled before any security 
can be applied. The security of all endpoints is controlled using the following settings:

{% include casproperties.html properties="cas.monitor.endpoints.endpoint" %}
