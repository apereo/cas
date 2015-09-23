---
layout: default
title: CAS - Service Management
---

# Service Management
The CAS service management facility allows CAS server administrators to declare and configure which services
(CAS clients) may make use of CAS in which ways. The core component of the service management facility is the
service registry, provided by the `ServiceRegistryDao` component, that stores one or more registered services
containing metadata that drives a number of CAS behaviors:

* Authorized services - Control which services may participate in a CAS SSO session.
* Forced authentication - Provides administrative control for forced authentication.
* Attribute release - Provide user details to services for authorization and personalization.
* Proxy control - Further restrict authorized services by granting/denying proxy authentication capability.
* Theme control - Define alternate CAS themese to be used for particular services.

The service management webapp is a Web application that may be deployed along side CAS that provides a GUI
to manage service registry data.


## Demo
The Service Management web application is available for demo at [https://jasigcasmgmt.herokuapp.com](https://jasigcasmgmt.herokuapp.com)


## Considerations
It is not required to use the service management facility explicitly. CAS ships with a default configuration that is
suitable for deployments that do not need or want to leverage the capabilities above. The default configuration allows
any service contacting CAS over https/imaps to use CAS and receive any attribute configured by an `IPersonAttributeDao`
bean.

A premier consideration around service management is whether to leverage the user interface. If the service management
webapp is used, then a `ServiceRegistryDao` that provides durable storage (e.g. `JpaServiceRegistryDaoImpl`) must be
used to preserve state across restarts.

It is perfectly acceptable to avoid the service management webapp Web application for managing registered service data.
In fact, configuration-driven methods (e.g. XML, JSON) may be preferable in environments where strict configuration
management controls are required.

## Registered Services

Registered services present the following metadata:

| Field                             | Description
|-----------------------------------+--------------------------------------------------------------------------------+
| `id`                              | Required unique identifier. In most cases this is managed automatically by the `ServiceRegistryDao`.
| `name`                            | Required name (255 characters or less).
| `description`                     | Optional free-text description of the service. (255 characters or less)
| `logo`                              | Optional path to an image file that is the logo for this service. The image will be displayed on the login page along with the service description and name.  
| `serviceId`                       | Required [Ant pattern](http://ant.apache.org/manual/dirtasks.html#patterns) or [regular expression](http://docs.oracle.com/javase/tutorial/essential/regex/) describing a logical service. A logical service defines one or more URLs where a service or services are located. The definition of the url pattern must be **done carefully** because it can open security breaches. For example, using Ant pattern, if you define the following service : `http://example.*/myService` to match `http://example.com/myService` and `http://example.fr/myService`, it's a bad idea as it can be tricked by `http://example.hostattacker.com/myService`. The best way to proceed is to define the more precise url patterns.
| `theme`                           | Optional [Spring theme](http://static.springsource.org/spring/docs/3.2.x/spring-framework-reference/html/mvc.html#mvc-themeresolver) that may be used to customize the CAS UI when the service requests a ticket. See [this guide](User-Interface-Customization.html) for more details.
| `proxyPolicy`                     | Determines whether the service is able to proxy authentication, not whether the service accepts proxy authentication.
| `evaluationOrder`                 | Required value that determines relative order of evaluation of registered services. This flag is particularly important in cases where two service URL expressions cover the same services; evaluation order determines which registration is evaluated first.
| `requiredHandlers`                | Set of authentication handler names that must successfully authenticate credentials in order to access the service.
| `attributeReleasePolicy`          | The policy that describes the set of attributes allows to be released to the application, as well as any other filtering logic needed to weed some out. See [this guide](../integration/Attribute-Release.html) for more details on attribute release and filters.
| `logoutType`                      | Defines how this service should be treated once the logout protocol is initiated. Acceptable values are `LogoutType.BACK_CHANNEL`, `LogoutType.FRONT_CHANNEL` or `LogoutType.NONE`. See [this guide](Logout-Single-Signout.html) for more details on logout.
| `usernameAttributeProvider`       | The provider configuration which dictates what value as the "username" should be sent back to the application. See [this guide](../integration/Attribute-Release.html) for more details on attribute release and filters.
| `accessStrategy`                  | The strategy configuration that outlines and access rules for this service. It describes whether the service is allowed, authorized to participate in SSO, or can be granted access from the CAS perspective based on a particular attribute-defined role, aka RBAC. See [this guide](../integration/Attribute-Release.html) for more details on attribute release and filters.  
| `publicKey`                  		| The public key associated with this service that is used to authorize the request by encrypting certain elements and attributes in the CAS validation protocol response, such as [the PGT](Configuring-Proxy-Authentication.html) or [the credential](../integration/ClearPass.html). See [this guide](../integration/Attribute-Release.html) for more details on attribute release and filters.  
| `logoutUrl`                  		| URL endpoint for this service to receive logout requests. See [this guide](Logout-Single-Signout.html) for more details

###Configure Service Access Strategy
The access strategy of a registered service provides fine-grained control over the service authorization rules. it describes whether the service is allowed to use the CAS server, allowed to participate in single sign-on authentication, etc. Additionally, it may be configured to require a certain set of principal attributes that must exist before access can be granted to the service. This behavior allows one to configure various attributes in terms of access roles for the application and define rules that would be enacted and validated when an authentication request from the application arrives.

####Components

#####`RegisteredServiceAccessStrategy`
This is the parent interface that outlines the required operations from the CAS perspective that need to be carried out in order to determine whether the service can proceed to the next step in the authentication flow.

#####`DefaultRegisteredServiceAccessStrategy`
The default access manager allows one to configure a service with the following properties:

| Field                             | Description
|-----------------------------------+--------------------------------------------------------------------------------+
| `enabled`                         | Flag to toggle whether the entry is active; a disabled entry produces behavior equivalent to a non-existent entry.
| `ssoEnabled`                      | Set to `false` to force users to authenticate to the service regardless of protocol flags (e.g. `renew=true`). This flag provides some support for centralized application of security policy.
| `requiredAttributes`              | A `Map` of required principal attribute names along with the set of values for each attribute. These attributes must be available to the authenticated Principal and resolved before CAS can proceed, providing an option for role-based access control from the CAS perspective. If no required attributes are presented, the check will be entirely ignored.
| `requireAllAttributes`            | Flag to toggle to control the behavior of required attributes. Default is `true`, which means all required attribute names must be present. Otherwise, at least one matching attribute name may suffice. Note that this flag only controls which and how many of the attribute **names** must be present. If attribute names satisfy the CAS configuration, at the next step at least one matching attribute value is required for the access strategy to proceed successfully.

<div class="alert alert-info"><strong>Are we sensitive to case?</strong><p>Note that comparison of principal/required attributes is case-sensitive. Exact matches are required for any individual attribute value.</p></div>

<div class="alert alert-info"><strong>Released Attributes</strong><p>Note that if the CAS server is configured to cache attributes upon release, all required attributes must also be released to the relying party. <a href="../integration/Attribute-Release.html">See this guide</a> for more info on attribute release and filters.</p></div>

####Configuration of Role-based Access Control
Some examples of RBAC configuration follow:

* Service is not allowed to use CAS:

{% highlight xml %}
<bean class="org.jasig.cas.services.RegexRegisteredService"
         p:id="10000001" p:name="HTTP and IMAP"
         p:serviceId="^(https?|imaps?)://.*">
    <property name="accessStrategy">
        <bean class="org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy"
                c:ssoEnabled="true"
                c:enabled="false"/>
    </property>
...
</bean>
{% endhighlight %}


* Service will be challenged to present credentials every time, thereby not using SSO:

{% highlight xml %}
<bean class="org.jasig.cas.services.RegexRegisteredService"
         p:id="10000001" p:name="HTTP and IMAP"
         p:serviceId="^(https?|imaps?)://.*">
    <property name="accessStrategy">
        <bean class="org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy"
                c:ssoEnabled="false"
                c:enabled="true"/>
    </property>
...
</bean>
{% endhighlight %}


* To access the service, the principal must have a `cn` attribute with the value of `admin` **AND** a
`givenName` attribute with the value of `Administrator`:

{% highlight xml %}
<bean class="org.jasig.cas.services.RegexRegisteredService"
         p:id="10000001" p:name="HTTP and IMAP"
         p:serviceId="^(https?|imaps?)://.*">
    <property name="accessStrategy">
        <bean class="org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy">
             <map>
                 <entry key="cn" value="admin" />
                 <entry key="givenName" value="Administrator" />
             </map>
        </bean>
    </property>
...
</bean>
{% endhighlight %}

* To access the service, the principal must have a `cn` attribute whose value is either of `admin`, `Admin` or `TheAdmin`.

{% highlight xml %}
<bean class="org.jasig.cas.services.RegexRegisteredService"
         p:id="10000001" p:name="HTTP and IMAP"
         p:serviceId="^(https?|imaps?)://.*">
    <property name="accessStrategy">
        <bean class="org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy">
            <map>
                <entry key="cn">
                    <list>
                           <value>admin</value>
                           <value>Admin</value>
                           <value>TheAdmin</value>
                    </list>
                </entry>
             </map>
        </bean>
    </property>
...
</bean>
{% endhighlight %}


* To access the service, the principal must have a `cn` attribute whose value is either of `admin`, `Admin` or `TheAdmin`,
OR the principal must have a `member` attribute whose value is either of `admins`, `adminGroup` or `staff`.

{% highlight xml %}
<bean class="org.jasig.cas.services.RegexRegisteredService"
         p:id="10000001" p:name="HTTP and IMAP"
         p:serviceId="^(https?|imaps?)://.*">
    <property name="accessStrategy">
        <bean class="org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy"
                p:requireAllAttributes="false">
            <map>
                <entry key="cn">
                    <list>
                        <value>admin</value>
                        <value>Admin</value>
                        <value>TheAdmin</value>
                    </list>
                </entry>
                <entry key="member">
                    <list>
                        <value>admins</value>
                        <value>adminGroup</value>
                        <value>staff</value>
                    </list>
                </entry>
             </map>
        </bean>
    </property>
...
</bean>
{% endhighlight %}


###Configure Proxy Authentication Policy
Each registered application in the registry may be assigned a proxy policy to determine whether the service is allowed for proxy authentication. This means that a PGT will not be issued to a service unless the proxy policy is configured to allow it. Additionally, the policy could also define which endpoint urls are in fact allowed to receive the PGT.

Note that by default, the proxy authentication is disallowed for all applications.

####Components

#####`RefuseRegisteredServiceProxyPolicy`
Disallows proxy authentication for a service. This is default policy and need not be configured explicitly.

#####`RegexMatchingRegisteredServiceProxyPolicy`
A proxy policy that only allows proxying to PGT urls that match the specified regex pattern.

{% highlight xml %}
<bean class="org.jasig.cas.services.RegexRegisteredService"
         p:id="10000001" p:name="HTTP and IMAP"
         p:description="Allows HTTP(S) and IMAP(S) protocols"
         p:serviceId="^(https?|imaps?)://.*" p:evaluationOrder="10000001">
    <property name="proxyPolicy">
        <bean class="org.jasig.cas.services.RegexMatchingRegisteredServiceProxyPolicy"
            c:pgtUrlPattern="^https?://.*" />
    </property>
</bean>
{% endhighlight %}

## Persisting Registered Service Data

######`InMemoryServiceRegistryDaoImpl`
CAS uses in-memory services management by default, with the registry seeded from registration beans wired via Spring.

{% highlight xml %}
<bean id="serviceRegistryDao"
      class="org.jasig.cas.services.InMemoryServiceRegistryDaoImpl"
      p:registeredServices-ref="registeredServicesList" />

<util:list id="registeredServicesList">
    <bean class="org.jasig.cas.services.RegexRegisteredService"
          p:id="1"
          p:name="HTTPS and IMAPS services on example.com"
          p:serviceId="^(https|imaps)://([A-Za-z0-9_-]+\.)*example\.com/.*"
          p:evaluationOrder="0" />
</util:list>

{% endhighlight %}

This component is _NOT_ suitable for use with the service management webapp since it does not persist data.
On the other hand, it is perfectly acceptable for deployments where the XML configuration is authoritative for
service registry data and the UI will not be used.

######`JsonServiceRegistryDao`

[See this guide](JSON-Service-Management.html) for more info please.

######`MongoServiceRegistryDao`

[See this guide](Mongo-Service-Management.html) for more info please.

######`LdapServiceRegistryDao`

[See this guide](LDAP-Service-Management.html) for more info please.

######`JpaServiceRegistryDaoImpl`

[See this guide](JPA-Service-Management.html) for more info please.

## Service Management Webapp
The Services Management web application is a standalone application that helps one manage service registrations and entries via a customizable user interface. The management web application *MUST* share the same registry configuration as
the CAS server itself so the entire system can load the same services data. To learn more about the management webapp,
[please see this guide](Installing-ServicesMgmt-Webapp.html).
