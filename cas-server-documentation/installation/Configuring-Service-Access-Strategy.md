---
layout: default
title: CAS - Configuring Service Access Strategy
---

# Configure Service Access Strategy
The access strategy of a registered service provides fine-grained control over the service authorization rules.
it describes whether the service is allowed to use the CAS server, allowed to participate in
single sign-on authentication, etc. Additionally, it may be configured to require a certain set of principal
attributes that must exist before access can be granted to the service. This behavior allows one to configure
various attributes in terms of access roles for the application and define rules that would be enacted and
validated when an authentication request from the application arrives.

## Components

### `RegisteredServiceAccessStrategy`
This is the parent interface that outlines the required operations from the CAS perspective that need to be carried out in order to determine whether the service can proceed to the next step in the authentication flow.

### `DefaultRegisteredServiceAccessStrategy`
The default access manager allows one to configure a service with the following properties:

| Field                             | Description
|-----------------------------------+--------------------------------------------------------------------------------+
| `enabled`                         | Flag to toggle whether the entry is active; a disabled entry produces behavior equivalent to a non-existent entry.
| `ssoEnabled`                      | Set to `false` to force users to authenticate to the service regardless of protocol flags (e.g. `renew=true`). This flag provides some support for centralized application of security policy.
| `requiredAttributes`              | A `Map` of required principal attribute names along with the set of values for each attribute. These attributes must be available to the authenticated Principal and resolved before CAS can proceed, providing an option for role-based access control from the CAS perspective. If no required attributes are presented, the check will be entirely ignored.
| `requireAllAttributes`            | Flag to toggle to control the behavior of required attributes. Default is `true`, which means all required attribute names must be present. Otherwise, at least one matching attribute name may suffice. Note that this flag only controls which and how many of the attribute **names** must be present. If attribute names satisfy the CAS configuration, at the next step at least one matching attribute value is required for the access strategy to proceed successfully.
| `unauthorizedRedirectUrl`         | Optional url to redirect the flow in case service access is not allowed.
| `caseInsensitive`                 | Indicates whether matching on required attribute values should be done in a case-insensitive manner. Default is `false`


<div class="alert alert-info"><strong>Are we sensitive to case?</strong><p>Note that comparison of principal/required attribute <strong>names</strong> is
case-sensitive. Exact matches are required for any individual attribute name.</p></div>

<div class="alert alert-info"><strong>Released Attributes</strong><p>Note that if the CAS server is configured to cache attributes upon release, all required attributes must also be released to the relying party. <a href="../integration/Attribute-Release.html">See this guide</a> for more info on attribute release and filters.</p></div>

### `TimeBasedRegisteredServiceAccessStrategy`
This access strategy is an extension of the default which additionally,
allows one to configure a service with the following properties:

| Field                             | Description
|-----------------------------------+--------------------------------------------------------------------------------+
| `startingDateTime`                | Indicates the starting date/time whence service access may be granted.  (i.e. `2015-10-11T09:55:16.552-07:00`)
| `endingDateTime`                  | Indicates the ending date/time whence service access may be granted.  (i.e. `2015-10-20T09:55:16.552-07:00`)

### `GrouperRegisteredServiceAccessStrategy`
Support is enabled by including the following dependency in the Maven WAR overlay:

{% highlight xml %}
<dependency>
  <groupId>org.jasig.cas</groupId>
  <artifactId>cas-server-integration-grouper</artifactId>
  <version>${cas.version}</version>
</dependency>
{% endhighlight %}

This access strategy attempts to locate Grouper groups for the CAS principal. The groups returned by Grouper
are collected as CAS attribtues and examines against the list of required attributes for service access.

The following properties are available:

| Field                             | Description
|-----------------------------------+--------------------------------------------------------------------------------+
| `groupField`                | Decides which attribute of the Grouper group should be used when converting the group to a CAS attribute. Possible values are `NAME`, `EXTENSION`, `DISPLAY_NAME`, `DISPLAY_EXTENSION`.

You will also need to ensure `grouper.client.properties` is available on the classpath:

{% highlight properties %}
grouperClient.webService.url = http://192.168.99.100:32768/grouper-ws/servicesRest
grouperClient.webService.login = banderson
grouperClient.webService.password = password

{% endhighlight %}


## Configuration of Access Control
Some examples of RBAC configuration follow:

* Service is not allowed to use CAS:

{% highlight json %}
{
  "@class" : "org.jasig.cas.services.RegexRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : false,
    "ssoEnabled" : true
  }
}
{% endhighlight %}


* Service will be challenged to present credentials every time, thereby not using SSO:

{% highlight json %}
{
  "@class" : "org.jasig.cas.services.RegexRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : true,
    "ssoEnabled" : false
  }
}
{% endhighlight %}


* To access the service, the principal must have a `cn` attribute with the value of `admin` **AND** a
`givenName` attribute with the value of `Administrator`:

{% highlight json %}
{
  "@class" : "org.jasig.cas.services.RegexRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : true,
    "ssoEnabled" : true,
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "admin" ] ],
      "givenName" : [ "java.util.HashSet", [ "Administrator" ] ]
    }
  }
}
{% endhighlight %}

* To access the service, the principal must have a `cn` attribute whose value is either of `admin`, `Admin` or `TheAdmin`.

{% highlight json %}
{
  "@class" : "org.jasig.cas.services.RegexRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : true,
    "ssoEnabled" : true,
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "admin, Admin, TheAdmin" ] ]
    }
  }
}
{% endhighlight %}


* To access the service, the principal must have a `cn` attribute whose value is either of `admin`, `Admin` or `TheAdmin`,
OR the principal must have a `member` attribute whose value is either of `admins`, `adminGroup` or `staff`.


{% highlight json %}
{
  "@class" : "org.jasig.cas.services.RegexRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : true,
    "requireAllAttributes" : false,
    "ssoEnabled" : true,
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "cn" : [ "java.util.HashSet", [ "admin, Admin, TheAdmin" ] ],
      "member" : [ "java.util.HashSet", [ "admins, adminGroup, staff" ] ]
    }
  }
}
{% endhighlight %}

* Service access is only allowed within `startingDateTime` and `endingDateTime`:

{% highlight json %}
{
  "@class" : "org.jasig.cas.services.RegexRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "test",
  "id" : 62,
  "accessStrategy" : {
    "@class" : "org.jasig.cas.services.TimeBasedRegisteredServiceAccessStrategy",
    "enabled" : true,
    "ssoEnabled" : true,
    "unauthorizedRedirectUrl" : "https://www.github.com",
    "startingDateTime" : "2015-11-01T13:19:54.132-07:00",
    "endingDateTime" : "2015-11-10T13:19:54.248-07:00"
  }
}
{% endhighlight %}

* Grouper access strategy based on group's display extension:

{% highlight json %}
{
  "@class" : "org.jasig.cas.services.RegexRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "test",
  "id" : 62,
  "accessStrategy" : {
    "@class" : "org.jasig.cas.grouper.services.GrouperRegisteredServiceAccessStrategy",
    "enabled" : true,
    "ssoEnabled" : true,
    "requireAllAttributes" : true,
    "requiredAttributes" : {
      "@class" : "java.util.HashMap",
      "memberOf" : [ "java.util.HashSet", [ "admin" ] ]
    },
    "groupField" : "DISPLAY_EXTENSION"
  },
}
{% endhighlight %}
