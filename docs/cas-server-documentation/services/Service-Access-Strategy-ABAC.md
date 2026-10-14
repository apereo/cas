---
layout: default
title: CAS - Service Access Strategy - ABAC
category: Services
---

{% include variables.html %}

# Service Access Strategy - ABAC

Attribute-based Access Control (ABAC) is an authorization model that evaluates principal/user attributes 
rather than roles to determine access. With ABAC, the access policies enforce access decisions based on the 
attributes of the authenticated subject/principal, resource, action, and environment involved in an access request. The **principal** is the user requesting 
access to a resource to perform an action. Principal attributes in a user profile include ID, job roles, group memberships, departmental and organizational 
memberships, management level, security clearance, and other identifying criteria.

<div class="alert alert-info">:information_source: <strong>Usage</strong><p>Note that comparison of 
principal/required attribute <strong>names</strong> is case-sensitive. Exact matches are required
for any individual attribute name. Furthermore, note that if the CAS server is configured to cache
attributes upon release, all required attributes must also be released to the
relying party. <a href="../integration/Attribute-Release.html">See this guide</a> for more info on
attribute release and filters.

</p></div>


The ABAC strategy allows one to configure a service with the following properties:

| Field                  | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
|------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `requiredAttributes`   | A `Map` of required principal attribute names along with the set of values for each attribute. These attributes **MUST** be available to the authenticated Principal and resolved before CAS can proceed, providing an option for role-based access control from the CAS perspective. If no required attributes are presented, the check will be entirely ignored.                                                                                                                                                                                     |
| `requireAllAttributes` | Controls how many attributes must match, for both required and rejected attributes. Default is `true`. For `requiredAttributes`, `true` means all required attribute names must be present and each must have a matching value; `false` means one present attribute with a matching value suffices. For `rejectedAttributes`, `true` means access is refused only when all rejected attribute names are present and each has a matching value; `false` means access is refused as soon as any one rejected attribute is present with a matching value. |
| `caseInsensitive`      | Indicates whether matching on required attribute values should be done in a case-insensitive manner. Default is `false`                                                                                                                                                                                                                                                                                                                                                                                                                                |
| `rejectedAttributes`   | A `Map` of rejected principal attribute names along with the set of values for each attribute. A principal carrying these attributes with matching values is refused access; whether all of them or any one of them must match is controlled by `requireAllAttributes`. If none is defined, the check is entirely ignored.                                                                                                                                                                                                                             |
     
You can also tune the ABAC strategy to conditionally activate and enforce the policy. [See this guide](Service-Access-Strategy-ABAC-Activation.html) for more info.


{% tabs accessstrategy %}

{% tab accessstrategy Required Attributes <i class="fa fa-id-card px-1"></i> %}

Control access using a `Map` of required principal attribute names along with the set of values for each attribute.

<div class="alert alert-info">:information_source: <strong>Supported Syntax</strong><p>Required values for a given attribute support 
regular expression patterns. For example, a <code>phone</code> attribute could
require a value pattern of <code>\d\d\d-\d\d\d-\d\d\d\d</code>. A pattern must match the entire attribute value,
so <code>admin</code> matches only <code>admin</code> and not <code>sysadmin</code>; use <code>.*admin.*</code>
to match part of a value.</p></div>

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

{% tab accessstrategy Optional Attributes <i class="fa fa-id-card px-1"></i> %}
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

{% tab accessstrategy <i class="fa fa-code px-1"></i>Groovy %}
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

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

{% endtab %}

{% tab accessstrategy Rejected Attributes <i class="fa fa-ban px-1"></i> %}
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

Since `requireAllAttributes` is `false` here, a matching `role` value alone refuses access. With `requireAllAttributes` set to `true`,
access would be refused only if the principal carried every rejected attribute with a matching value, so a policy that should refuse
access on any one of several rejected attributes must set `requireAllAttributes` to `false`.

<div class="alert alert-info">:information_source: <strong>Supported Syntax</strong><p>Rejected values for a given attribute support regular 
expression patterns. For example, a <code>role</code> attribute could
be designed with a value pattern of <code>admin-.*</code>. As with required values, a pattern must match the
entire attribute value.</p></div>
{% endtab %}

{% endtabs %}
