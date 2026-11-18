# Attribute Release Caching

Source: https://apereo.github.io/cas/development/integration/Attribute-Release-Caching.html

By default, [resolved attributes](https://apereo.github.io/cas/development/integration/Attribute-Resolution.html) are cached to the length of the SSO session. If there are any attribute value changes since the commencement of SSO session, the changes are not reflected and returned back to the service upon release time.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.attribute-repository.core.aggregation` (Optional; type `PrincipalAttributesCoreProperties.AggregationStrategyTypes`; default `MERGE`): Indicates how the results of multiple attribute repositories should be aggregated together. Available values are as follows: - `MERGE`: Default. Designed to query multiple repositories in order and merge the results into a single result set. - `CASCADE`: Query multiple repositories in order and merge the results into a single result set. As each repository is queried the attributes from the first query in the result set are used as the query for the next repository.
- `cas.authn.attribute-repository.core.default-attributes-to-release` (Optional; type `Set<String>`): CAS provides the ability to release a bundle of principal attributes to all services by default. This bundle is not defined on a per-service basis and is always combined with attributes produced by the specific release policy of the service, such that for instance, you can devise rules to always release `givenName` and `cn` to every application, and additionally allow other specific principal attributes for only some applications per their attribute release policy.
- `cas.authn.attribute-repository.core.expiration-time` (Optional; type `Integer`; default `30`): Indicates the global cache expiration period, once attributes are fetched from the underlying attribute repository. A zero or negative value indicates that no attribute caching should take place where attributes must always be fetched from the source.
- `cas.authn.attribute-repository.core.expiration-time-unit` (Optional; type `String`): Expiration caching time unit for attributes.
- `cas.authn.attribute-repository.core.maximum-cache-size` (Optional; type `Integer`; default `10000`): Indicates the global cache size used to store attributes retrieved from the attribute repository.
- `cas.authn.attribute-repository.core.merger` (Optional; type `PrincipalAttributesCoreProperties.MergingStrategyTypes`; default `REPLACE`): Merging strategies can be used to resolve conflicts when the same attributes are found from multiple sources. A merging strategy is used to handle conflicts for both principal attributes as well as those that are captured by the authentication attempt. Conflicts arise when the multiple attribute sources or repositories produce the same attribute with the same name, or when there are multiple legs in an authentication flow that produce the same attribute as authentication metadata for each leg of the attempt (i.e. when going through MFA flows). Available values are as follows: - `REPLACE`: Replace attributes. Overwrites existing attribute values, if any. - `ADD`: Add attributes. Retains existing attribute values if any, and ignores values from subsequent sources in the resolution chain. - `SOURCE`: No merging. Doesn't merge attributes, and returns the original collection of attributes as passed. - `DESTINATION`: No merging. Ignore the collection of original attributes that are passed and always favor what is supplied as a subsequent source and an override. - `MULTIVALUED`: Multivalued attributes. Combines all values into a single attribute, essentially creating a multi-valued attribute.
- `cas.authn.attribute-repository.core.recover-exceptions` (Optional; type `Boolean`; default `true`): Recover from LDAP exceptions and continue with partial results. Otherwise, die and do not allow to log in.
- `cas.authn.attribute-repository.core.require-all-repository-sources` (Optional; type `Boolean`; default `false`): In the event that multiple attribute repositories are defined, setting this option to `true` forces all repositories to produce a person object. If any of the repositories fails to produce a person or person attributes, the resolution engine will halt to short-circuit the process, failing to resolve the person altogether.
- `cas.authn.attribute-repository.core.stop-cascading-when-no-initial-results` (Optional; type `Boolean`; default `true`): When `#aggregation` is set to `AggregationStrategyTypes#CASCADE`, this setting controls whether subsequent attribute repositories need to be contacted for person attributes, if the first attribute repository's query does not produce any results.

## Principal Attribute Repositories

The following settings are shared by all principal attribute repositories:

| Name | Value |
| --- | --- |
| `mergingStrategy` | Indicate the merging strategy when combining attributes from multiple sources. Accepted values are `MULTIVALUED`, `ADD`, `NONE`, `MULTIVALUED` |
| `attributeRepositoryIds` | A `Set` of attribute repository identifiers to consult for attribute resolution at release time. |
| `ignoreResolvedAttributes` | Ignore the collection of attributes that may have been resolved during the principal resolution phase, typically via attribute repositories. |

The following caching strategies are offered by CAS:

**Default**

The default relationship between a CAS `Principal` and the underlying attribute repository source, such that principal attributes are kept as they are without any additional processes to evaluate and update them. This need not be configured explicitly.

**Caching**

The relationship between a CAS `Principal` and the underlying attribute repository source, that describes how and at what length the CAS `Principal` attributes should be cached. Upon attribute release time, this component is consulted to ensure that appropriate attribute values are released to the scoped service, per the cache expiration policy. If the expiration policy has passed, the underlying attribute repository source will be consulted to figure out the available set of attributes.

This component also has the ability to resolve conflicts between existing principal attributes and those that are retrieved from repository source via a `mergingStrategy` property. This is useful if you want to preserve the collection of attributes that are already available to the principal that were retrieved from a different place during the authentication event, etc.

> **Caching Upon Release**
>
> Note that the policy is only consulted at release time, upon a service ticket validation event. If there are any custom webflows and such that wish to rely on the resolved `Principal` AND also wish to receive an updated set of attributes, those components must consult the underlying source directory without relying on the `Principal`.

Sample configuration follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "principalAttributesRepository" : {
      "@class" : "org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository",
      "timeUnit" : "HOURS",
      "expiration" : 2,
      "mergingStrategy" : "NONE"
    }
  }
}
```

### Merging Strategies

By default, no merging strategy takes place, which means the principal attributes are always ignored and attributes from the source are always returned. But any of the following merging strategies may be a suitable option:

**Merge**

Attributes with the same name are merged into multi-valued lists.

For example:

1. Principal has attributes `{email=eric.dalquist@example.com, phone=123-456-7890}`
2. Source has attributes `{phone=[111-222-3333, 000-999-8888], office=3233}`
3. The resulting merged would have attributes: `{email=eric.dalquist@example.com, phone=[123-456-7890, 111-222-3333, 000-999-8888], office=3233}`

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "principalAttributesRepository" : {
      "@class" : "org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository",
      "timeUnit" : "HOURS",
      "expiration" : 2,
      "mergingStrategy" : "MULTIVALUED"
    }
  }
}
```

**Add**

Attributes are merged such that attributes from the source that don’t already exist for the principal are produced.

For example:

1. Principal has attributes `{email=eric.dalquist@example.com, phone=123-456-7890}`
2. Source has attributes `{phone=[111-222-3333, 000-999-8888], office=3233}`
3. The resulting merged would have attributes: `{email=eric.dalquist@example.com, phone=123-456-7890, office=3233}`

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "principalAttributesRepository" : {
      "@class" : "org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository",
      "timeUnit" : "HOURS",
      "expiration" : 2,
      "mergingStrategy" : "ADD"
    }
  }
}
```

**Replace**

Attributes are merged such that attributes from the source always replace principal attributes.

For example:

1. Principal has attributes `{email=eric.dalquist@example.com, phone=123-456-7890}`
2. Source has attributes `{phone=[111-222-3333, 000-999-8888], office=3233}`
3. The resulting merged would have attributes: `{email=eric.dalquist@example.com, phone=[111-222-3333, 000-999-8888], office=3233}`

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "principalAttributesRepository" : {
      "@class" : "org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository",
      "timeUnit" : "HOURS",
      "expiration" : 2,
      "mergingStrategy" : "REPLACE"
    }
  }
}
```
