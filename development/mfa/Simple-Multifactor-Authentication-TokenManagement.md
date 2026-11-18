# Simple Multifactor Authentication - Token Management

Source: https://apereo.github.io/cas/development/mfa/Simple-Multifactor-Authentication-TokenManagement.html

Token management and issuance can be handled by CAS directly or can be outsources to external systems and services.

By default, tokens issued by CAS are tracked using the [ticket registry](https://apereo.github.io/cas/development/ticketing/Configuring-Ticketing-Components.html) and are assigned a configurable expiration policy controlled via CAS settings. In this option, CAS itself is in charge of managing and validating tokens using pre-configured policies and components.

> **Usage Warning!**
>
> Note that generating codes with a small length (i.e. smaller than `6` characters or digits) may lead to code creation collisions, typically under rather high load. CAS will attempt to retry and generate a unique code for given attempt, but may ultimately give up if a unique code cannot be generated and reserved.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.simple.token.core.time-to-kill-in-seconds` (Optional; type `Long`; default `30`): Time in seconds that CAS tokens should be considered live in CAS server.
- `cas.authn.mfa.simple.token.core.token-length` (Optional; type `Integer`; default `6`): The length of the generated token. Note that generating codes with a small length (i.e. smaller than `6` characters or digits) may lead to code creation collisions, typically under rather high load. CAS will attempt to retry and generate a unique code for given attempt, but may ultimately give up if a unique code cannot be generated and reserved.
- `cas.authn.mfa.simple.token.core.token-type` (Optional; type `CoreCasSimpleMultifactorAuthenticationTokenProperties.TokenType`; default `NUMERIC`): This setting determines the type of characters used in the token. Available values are as follows: - `NUMERIC`: Token type is numeric. - `ALPHANUMERIC`: Token type is alphanumeric. - `ALPHABETIC`: Token type is alphabetic.
