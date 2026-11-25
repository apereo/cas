# Attribute Release

Source: https://apereo.github.io/cas/development/integration/Attribute-Release.html

Attributes are returned to scoped services and pass through a two-step process:

- [Attribute Resolution](https://apereo.github.io/cas/development/integration/Attribute-Resolution.html): Done at the time of establishing the principal, *usually* via `PrincipalResolver` components where attributes are resolved from various sources.
- Attribute Release: Adopters must explicitly configure attribute release for services in order for the resolved attributes to be released to a service in the validation response.

> **Service Management**
>
> Attribute release may also be configured via the [Service Management tool](https://apereo.github.io/cas/development/services/Service-Management.html).

## Principal-Id Attribute

Decide how CAS-protected applications should receive the authenticated userid. See [this guide](https://apereo.github.io/cas/development/integration/Attribute-Release-PrincipalId.html) for more info.

## Attribute Release Policy

Decide how CAS should release attributes to applications. See [this guide](https://apereo.github.io/cas/development/integration/Attribute-Release-Policies.html) for more info.

## Attribute Consent

Provide the ability to enforce user consent to attribute release. See [this guide](https://apereo.github.io/cas/development/integration/Attribute-Release-Consent.html) for more info.

## Caching Attributes

Control how resolved attributes by CAS should be cached. See [this guide](https://apereo.github.io/cas/development/integration/Attribute-Release-Caching.html) for more info.

## Encrypting Attributes

CAS by default supports the ability to encrypt certain attributes, such as the proxy-granting ticket and the credential conditionally. The default implementation of the attribute encoder will use a per-service key-pair to encrypt sensitive attributes. See [this guide](https://apereo.github.io/cas/development/services/Service-Management.html) to learn more.

## Attribute Definitions

CAS attributes may be decorated with additional metadata which can later be used depending on the requirements of the protocol and nature of the integration with a target application. To learn more, please [see this guide](https://apereo.github.io/cas/development/integration/Attribute-Definitions.html).
