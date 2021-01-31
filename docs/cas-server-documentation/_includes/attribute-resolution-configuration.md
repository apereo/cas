Control the set of authentication attributes that are retrieved by the principal resolution process,
from attribute sources unless noted otherwise by the specific authentication scheme.

If multiple attribute repository sources are defined, they are added into a list
and their results are cached and merged.

{% include casproperties.html properties="cas.authn.attribute-repository.core" %}

<div class="alert alert-info"><strong>Remember This</strong><p>Note that in certain cases,
CAS authentication is able to retrieve and resolve attributes from the authentication 
source in the same authentication request, which would
eliminate the need for configuring a separate attribute repository specially 
if both the authentication and the attribute source are the same.
Using separate repositories should be required when sources are different, 
or when there is a need to tackle more advanced attribute
resolution use cases such as cascading, merging, etc.</p></div>

Attributes for all sources are defined in their own individual block.
CAS does not care about the source owner of attributes. It finds them where they can be found and otherwise, it moves on.
This means that certain number of attributes can be resolved via one source and the remaining attributes
may be resolved via another. If there are commonalities across sources, the merger shall decide the final result and behavior.

Note that attribute repository sources, if/when defined, execute in a specific order.
This is important to take into account when attribute merging may take place.

Note that if no *explicit* attribute mappings are defined, all permitted attributes on the record
may be retrieved by CAS from the attribute repository source and made available to the principal. On the other hand,
if explicit attribute mappings are defined, then *only mapped attributes* are retrieved.


The following merging strategies can be used to resolve conflicts when the same attribute are found from multiple sources:

| Type                    | Description
|-------------------------|--------------------------------------------------------------------------------------
| `REPLACE`               | Overwrites existing attribute values, if any.
| `ADD`                   | Retains existing attribute values if any, and ignores values from subsequent sources in the resolution chain.
| `MULTIVALUED`           | Combines all values into a single attribute, essentially creating a multi-valued attribute.
| `NONE`                  | Do not merge attributes, only use attributes retrieved during authentication.

The following aggregation strategies can be used to resolve and merge attributes
when multiple attribute repository sources are defined to fetch data:

| Type            | Description
|-----------------|-------------------------------------------------------------
| `MERGE`         | Default. Query multiple repositories in order and merge the results into a single result set.
| `CASCADE`       | Same as above; results from each query are passed down to the next attribute repository source.
