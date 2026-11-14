# Attribute Release Policy - Default Bundle

Source: https://apereo.github.io/cas/development/integration/Attribute-Release-Policy-DefaultBundle.html

CAS provides the ability to release a bundle of principal attributes to all services by default. This bundle is not defined on a per-service basis and is always combined with attributes produced by the specific release policy of the service, such that for instance, you can devise rules to always release `givenName` and `cn` to every application, and additionally allow other specific principal attributes for only some applications per their attribute release policy.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.attribute-repository.core.default-attributes-to-release` (Optional; type `Set<String>`): CAS provides the ability to release a bundle of principal attributes to all services by default. This bundle is not defined on a per-service basis and is always combined with attributes produced by the specific release policy of the service, such that for instance, you can devise rules to always release `givenName` and `cn` to every application, and additionally allow other specific principal attributes for only some applications per their attribute release policy.
