---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Service Access Strategy - ABAC

Attribute-based Access Control (ABAC) is an authorization model that evaluates principal/user attributes 
rather than roles to determine access. With ABAC, the access policies enforce access decisions based on the 
attributes of the authenticated subject/principal, resource, action, and environment involved in an access request. The **principal** is the user requesting 
access to a resource to perform an action. Principal attributes in a user profile include ID, job roles, group memberships, departmental and organizational 
memberships, management level, security clearance, and other identifying criteria.

The ABAC strategy allows one to configure a service with the following properties:

| Field                     | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `requiredAttributes`      | A `Map` of required principal attribute names along with the set of values for each attribute. These attributes **MUST** be available to the authenticated Principal and resolved before CAS can proceed, providing an option for role-based access control from the CAS perspective. If no required attributes are presented, the check will be entirely ignored.                                                                                                              |
| `requireAllAttributes`    | Flag to toggle to control the behavior of required attributes. Default is `true`, which means all required attribute names must be present. Otherwise, at least one matching attribute name may suffice. Note that this flag only controls which and how many of the attribute **names** must be present. If attribute names satisfy the CAS configuration, at the next step at least one matching attribute value is required for the access strategy to proceed successfully. |
| `caseInsensitive`         | Indicates whether matching on required attribute values should be done in a case-insensitive manner. Default is `false`                                                                                                                                                                                                                                                                                                                                                         |
| `rejectedAttributes`      | A `Map` of rejected principal attribute names along with the set of values for each attribute. These attributes **MUST NOT** be available to the authenticated Principal so that access may be granted. If none is defined, the check is entirely ignored.                                                                                                                                                                                                                      |
     
You can also tune the ABAC strategy to conditionally activate and enforce 
the policy. [See this guide](Service-Access-Strategy-ABAC-Activation.html) for more info.


{% tabs accessstrategy %}

{% tab accessstrategy Required Attributes %}

Control access using a `Map` of required principal attribute names along with the set of values for each attribute.

<div class="alert alert-info"><strong>Supported Syntax</strong><p>Required values for a given attribute support 
regular expression patterns. For example, a <code>phone</code> attribute could
require a value pattern of <code>\d\d\d-\d\d\d-\d\d\d\d</code>.</p></div>

To access the service, the principal must have a `cn` attribute with the value of `admin` **AND** a
`givenName` attribute with the value of `Administrator`:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : true,
    "ssoEnabled" : true,
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "admin" ] ],
      "givenName" : [ "java.util.HashSet", [ "Administrator" ] ]
    }
  }
}
```
{% endtab %}

{% tab accessstrategy Optional Attributes %}
To access the service, the principal must have a `cn` attribute with the value of `admin` **OR** a
`givenName` attribute with the value of `Administrator`:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : true,
    "ssoEnabled" : true,
    "requireAllAttributes": false,
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "admin" ] ],
      "givenName" : [ "java.util.HashSet", [ "Administrator" ] ]
    }
  }
}
```

To access the service, the principal must have a `cn` attribute whose value is either `admin`, `Admin` or `TheAdmin`.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : true,
    "ssoEnabled" : true,
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "admin", "Admin", "TheAdmin" ] ]
    }
  }
}
```
{% endtab %}

{% tab accessstrategy Combined Conditions %}
To access the service, the principal must have a `cn` attribute whose value is either `admin`, `Admin` or `TheAdmin`,
**OR** the principal must have a `member` attribute whose value is either `admins`, `adminGroup` or `staff`.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : true,
    "requireAllAttributes" : false,
    "ssoEnabled" : true,
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "admin", "Admin", "TheAdmin" ] ],
      "member" : [ "java.util.HashSet", [ "admins", "adminGroup", "staff" ] ]
    }
  }
}
```
{% endtab %}

{% tab accessstrategy Groovy %}
To access the service, the principal must have a `cn` attribute whose values must contain `admin`
and the overall set of resolved principal attributes must already have found an attribute for `name`.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : true,
    "requireAllAttributes" : false,
    "ssoEnabled" : true,
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ 
        "groovy { return attributes.containsKey('name') && currentValues.contains('admin') }" 
      ]]
    }
  }
}
```
{% endtab %}

{% tab accessstrategy Rejected Attributes %}
To access the service, the principal must have a `cn` attribute whose value
is either `admin`, `Admin` or `TheAdmin`, OR the principal must have a `member` attribute
whose value is either `admins`, `adminGroup` or `staff`. The principal also must not have an
attribute `role` whose value matches the pattern `deny.+`.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : true,
    "requireAllAttributes" : false,
    "ssoEnabled" : true,
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "admin", "Admin", "TheAdmin" ] ],
      "member" : [ "java.util.HashSet", [ "admins", "adminGroup", "staff" ] ]
    },
    "rejectedAttributes" : {
      "@class" : "java.util.HashMap",
      "role" : [ "java.util.HashSet", [ "deny.+" ] ]
    }
  }
}
```

<div class="alert alert-info"><strong>Supported Syntax</strong><p>Rejected values for a given attribute support regular 
expression patterns. For example, a <code>role</code> attribute could
be designed with a value value pattern of <code>admin-.*</code>.</p></div>
{% endtab %}

{% endtabs %}
