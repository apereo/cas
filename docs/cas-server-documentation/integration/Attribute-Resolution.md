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

## Administrative Endpoints

The following endpoints are provided by CAS:
 
| Endpoint                 | Description
|--------------------------|------------------------------------------------
| `resolveAttributes/{name}`    | Invoke the CAS [attribute resolution](Attribute-Resolution.html) engine to locate attributes for `{name}`.

## Person Directory

A framework for resolving persons and attributes from a variety of underlying sources.
It consists of a collection of components that retrieve, cache, resolve, aggregate,
merge person attributes from JDBC, LDAP and more.

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

{% include {{ version }}/attribute-resolution-configuration.md %}

### Stub
     
To learn more, please [see this guide](Attribute-Resolution-Stub.html).

### LDAP

To learn more, please [see this guide](Attribute-Resolution-LDAP.html).

### Groovy

To learn more, please [see this guide](Attribute-Resolution-Groovy.html).

### JSON

To learn more, please [see this guide](Attribute-Resolution-JSON.html).

### REST

To learn more, please [see this guide](Attribute-Resolution-REST.html).

### Grouper

To learn more, please [see this guide](Attribute-Resolution-Grouper.html).

### Couchbase

To learn more, please [see this guide](Attribute-Resolution-Couchbase.html).

### Python/Javascript/Groovy

To learn more, please [see this guide](Attribute-Resolution-Scripted.html).

### Redis

To learn more, please [see this guide](Attribute-Resolution-Redis.html).

### Microsoft Azure Active Directory

To learn more, please [see this guide](Attribute-Resolution-AzureAD.html).

### JDBC

To learn more, please [see this guide](Attribute-Resolution-JDBC.html).
