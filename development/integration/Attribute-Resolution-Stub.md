# Stub Attribute Resolution

Source: https://apereo.github.io/cas/development/integration/Attribute-Resolution-Stub.html

The following configuration describes how to fetch and retrieve attributes from Stub attribute repositories.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.attribute-repository.stub.attributes` (Optional; type `Map<String,String>`): Static attributes that need to be mapped to a hardcoded value belong here. The structure follows a key-value pair where key is the attribute name and value is the attribute value. The key is the attribute fetched from the source and the value is the attribute name CAS should use for virtual renames. Attributes may be allowed to be virtually renamed and remapped. The key in the attribute map is the original attribute, and the value should be the virtually-renamed attribute.
- `cas.authn.attribute-repository.stub.id` (Optional; type `String`): A value can be assigned to this field to uniquely identify this resolver.
- `cas.authn.attribute-repository.stub.order` (Optional; type `Integer`): The order of this attribute repository in the chain of repositories. Can be used to explicitly position this source in chain and affects merging strategies.
- `cas.authn.attribute-repository.stub.state` (Optional; type `AttributeRepositoryStates`; default `active`): Whether attribute resolution based on this source is enabled. Available values are as follows: - `ACTIVE`: Active and enabled repository, and is invoked by default automatically. - `DISABLED`: Attribute repository is disabled and will not be used to resolve people and attributes. - `STANDBY`: Repository is in a semi-enabled state, waiting to be called only on-demand when explicitly asked for and will not be registered into the resolution plan.

## Multitenancy

Configuration settings for stub attribute resolution can be specified in a multitenant environment. Please [review this guide](https://apereo.github.io/cas/development/multitenancy/Multitenancy-Tenant-Properties.html) for more information.
