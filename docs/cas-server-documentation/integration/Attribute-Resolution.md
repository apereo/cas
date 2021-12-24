---
layout: default
title: CAS - Attribute Resolution
category: Attributes
---

{% include variables.html %}

# Attribute Resolution

Attribute resolution strategies are controlled by
the [Person Directory project](https://github.com/apereo/person-directory).
The Person Directory dependency is automatically bundled with the CAS server. Therefore,
declaring an additional dependency will not be required.
This Person Directory project supports both LDAP and JDBC attribute resolution,
caching, attribute aggregation from multiple attribute sources, etc.

<div class="alert alert-info"><strong>Default Caching Policy</strong><p>By default,
attributes are cached to the length of the SSO session.
This means that while the underlying component provided by Person Directory may have
a different caching model, attributes by default and from
a CAS perspective will not be refreshed and retrieved again on subsequent requests
as long as the SSO session exists.</p></div>

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="resolveAttributes" casModule="cas-server-support-reports" %}

## Person Directory

A framework for resolving persons and attributes from a variety of underlying sources.
It consists of a collection of components that retrieve, cache, resolve, aggregate,
merge person attributes from JDBC, LDAP and more.

{% include_cached casproperties.html properties="cas.person-directory" %}

Attribute sources are defined and configured to describe the global set of attributes to be fetched
for each authenticated principal. That global set of attributes is then filtered by the
service manager according to service-specific attribute release rules.

Note that each attribute repository source can be assigned a unique 
identifier to be used for additional filtering. The attribute resolution engine
provided by Person Directory can also be configured to only consult not 
all but a selection of attribute repository sources, *deferring* the task
of attribute retrieval for later phases in the authentication process, 
such as [releasing attributes](Attribute-Release-Caching.html).

<div class="alert alert-info"><strong>Principal Resolution</strong><p>Note that in most if not all cases,
CAS authentication is able to retrieve and resolve attributes from the authentication source, which would
eliminate the need for configuring a separate resolver specially if 
both the authentication and the attribute source are the same.
Using separate resolvers should only be required when sources are 
different, or when there is a need to tackle more advanced attribute
resolution use cases such as cascading, merging, etc. 
<a href="../installation/Configuring-Principal-Resolution.html">See this guide</a> for more info.</p></div>

The goal of the resolver is to construct a final identifiable 
authenticated principal for CAS which carries a number of attributes inside it.
The behavior of the person-directory resolver is such that it attempts
to locate the principal id, which in most cases is the same thing as the credential
id provided during authentication or it could be noted by a custom 
attribute. Then the resolver starts to construct attributes from attribute 
repositories defined. If it realizes that a custom attribute is used to determine the principal id AND the same attribute 
is also set to be collected into the final set of attributes, it 
will then remove that attribute from the final collection.

Note that by default, CAS auto-creates attribute repository sources that are appropriate for LDAP, JDBC, etc.
If you need something more, you will need to resort to more elaborate measures of defining the bean configuration.

More about the Person Directory and its configurable 
sources [can be found here](https://github.com/apereo/person-directory).

### Overview

Control the set of authentication attributes that are retrieved by the principal resolution process,
from attribute sources unless noted otherwise by the specific authentication scheme.

If multiple attribute repository sources are defined, they are added into a list
and their results are cached and merged.

{% include_cached casproperties.html properties="cas.authn.attribute-repository.core" %}

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

| Type          | Description                                                                                                   |
|---------------|---------------------------------------------------------------------------------------------------------------|
| `REPLACE`     | Overwrites existing attribute values, if any.                                                                 |
| `ADD`         | Retains existing attribute values if any, and ignores values from subsequent sources in the resolution chain. |
| `MULTIVALUED` | Combines all values into a single attribute, essentially creating a multi-valued attribute.                   |
| `NONE`        | Do not merge attributes, only use attributes retrieved during authentication.                                 |

The following aggregation strategies can be used to resolve and merge attributes
when multiple attribute repository sources are defined to fetch data:

| Type      | Description                                                                                                                                                                                        |
|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `MERGE`   | Default. Query multiple repositories in order and merge the results into a single result set.                                                                                                      |
| `CASCADE` | Same as above; results from each query are passed down to the next attribute repository source. If the first repository queried has no results, no further attribute repositories will be queried. |

### Sources

The following options may be used to fetch attributes in CAS.

| Source         | Reference                                           
|---------------------------------------------------------------------------------
| Stub           | [See this guide](Attribute-Resolution-Stub.html).   
| LDAP           | [See this guide](Attribute-Resolution-LDAP.html).   
| Groovy         | [See this guide](Attribute-Resolution-Groovy.html).   
| REST           | [See this guide](Attribute-Resolution-REST.html).   
| Grouper        | [See this guide](Attribute-Resolution-Grouper.html).   
| Couchbase      | [See this guide](Attribute-Resolution-Couchbase.html).   
| Redis          | [See this guide](Attribute-Resolution-Redis.html).   
| JDBC           | [See this guide](Attribute-Resolution-JDBC.html).
| OKTA           | [See this guide](Attribute-Resolution-Okta.html).
| Custom         | [See this guide](Attribute-Resolution-Custom.html).
| Python/Javascript/Groovy          | [See this guide](Attribute-Resolution-Scripted.html).   
| Microsoft Azure Active Directory  | [See this guide](Attribute-Resolution-AzureAD.html).   
