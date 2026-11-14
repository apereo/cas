# Webflow Single Sign-on & Services

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-Services.html

By default, CAS will present a generic success page if the initial authentication request does not identify the target application. In some cases, the ability to login to CAS without logging in to a particular service may be considered a misfeature because in practice, too few users and institutions are prepared to understand, brand, and support what is at best a fringe use case of logging in to CAS for the sake of establishing an SSO session without logging in to any CAS-reliant service.

As such, CAS optionally allows adopters to not bother to prompt for credentials when no target application is presented and instead presents a message when users visit CAS directly without specifying a service.

The following settings and properties are available from the CAS configuration catalog:

- `cas.sso.services.allow-missing-service-parameter` (Optional; type `Boolean`; default `true`): Flag that indicates whether to allow SSO session with a missing target service. By default, CAS will present a generic success page if the initial authentication request does not identify the target application. In some cases, the ability to login to CAS without logging in to a particular service may be considered a misfeature because in practice, too few users and institutions are prepared to understand, brand, and support what is at best a fringe use case of logging in to CAS for the sake of establishing an SSO session without logging in to any CAS-reliant service.
- `cas.sso.services.required-service-pattern` (Optional; type `String`): A regular expression pattern that represents an application which must have established a session with CAS already before access to other applications can be allowed by CAS. This is the initial mandatory/required application with which the user must start before going anywhere else. Services that establish a session with CAS typically do so by receiving a service ticket from CAS.
