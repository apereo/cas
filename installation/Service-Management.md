---
layout: default
title: CAS - Service Management
---
<a name="ServiceManagement">  </a>
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

The service management console is a Web application that may be deployed along side CAS that provides a GUI
to manage service registry data.

<a name="Considerations">  </a>
## Considerations
It is not required to use the service management facility explicitly. CAS ships with a default configuration that is
suitable for deployments that do not need or want to leverage the capabilities above. The default configuration allows
any service contacting CAS over https/imaps to use CAS and receive any attribute configured by an `IPersonAttributeDao`
bean.


A premier consideration around service management is whether to leverage the user interface. If the service management
console is used, then a `ServiceRegistryDao` that provides durable storage (e.g. `JpaServiceRegistryDaoImpl`) must be
used to preserve state across restarts.

It is perfectly acceptable to avoid the service management console Web application for managing registered service data.
In fact, configuration-driven methods (e.g. XML, JSON) may be preferable in environments where strict configuration
management controls are required.

<a name="RegisteredServices">  </a>
## Registered Services

Registered services present the following metadata:

* _ID_ (`id`) - Required unique identifier. In most cases this is managed automatically by the `ServiceRegistryDao`.
* _Name_ (`name`) - Required name (255 characters or less).
* _Description_ (`description`) - Optional free-text description of the service. (255 characters or less)
* _Service URL_ (`serviceId`) - Required [Ant pattern](http://ant.apache.org/manual/dirtasks.html#patterns) or
[regular expression](http://docs.oracle.com/javase/tutorial/essential/regex/) describing a logical service. A logical
service defines one or more URLs where a service or services are located.
* _Theme Name_ (`theme`) - Optional [Spring theme](http://static.springsource.org/spring/docs/3.2.x/spring-framework-reference/html/mvc.html#mvc-themeresolver)
that may be used to customize the CAS UI when the service requests a ticket.
* _Enabled_ (`enabled`) - Flag to toggle whether the entry is active; a disabled entry produces behavior equivalent
to a non-existent entry.
* _SSO Participant_ (`ssoEnabled`) - Set to false to force users to authenticate to the service regardless of protocol flags (e.g. renew).
This flag provides some support for centralized application of security policy.
* _Anonymous Access_ (`anonymousAccess`) - Set to true to provide an opaque identifier for the username instead of
the principal ID. The default behavior (false) is to release the principal ID. The identifier conforms to the
requirements of the [eduPersonTargetedID](http://www.incommon.org/federation/attributesummary.html#eduPersonTargetedID)
attribute.
* _Allowed to Proxy_ (`allowedToProxy`) - True to allow proxy authentication, false otherwise. Note that this determines
whether the service is able to proxy authentication, not whether the service accepts proxy authentication.
* _User Attributes_ (`allowedAttributes`) - Optional field that allows restricting the global set of user attributes
on a per-service basis. Only the attributes specified in this field will be released.
* _Ignore Attribute Management_ (`ignoreAttributes`) - True to ignore per-service attribute management, which causes the value of _User Attributes_ (`allowedAttributes`) to be ignored.
* _Evaluation Order_ (`evaluationOrder`) - Required value that determines relative order of evaluation of registered services. This flag is particularly important in cases where two service URL expressions cover the same services;
evaluation order determines which registration is evaluated first.

<a name="PersistingRegisteredServiceData">  </a>
## Persisting Registered Service Data

<a name="InMemoryServiceRegistryDaoImpl">  </a>
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

This component is _NOT_ suitable for use with the service managment console since it does not persist data.
On the other hand, it is perfectly acceptable for deployments where the XML configuration is authoritative for
service registry data and the UI will not be used.

<a name="JpaServiceRegistryDaoImpl">  </a>
######`JpaServiceRegistryDaoImpl`
Stores registered service data in a database; the preferred choice when using the service management console.
The following configuration template may be applied to `deployerConfigContext.xml` to provide for persistent
registered service storage. The configuration assumes a `dataSource` bean is defined in the context.

{% highlight xml %}
<bean id="factoryBean"
      class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean"
      p:dataSource-ref="dataSource"
      p:jpaVendorAdapter-ref="jpaVendorAdapter"
      p:packagesToScan-ref="packagesToScan">
    <property name="jpaProperties">
      <props>
        <prop key="hibernate.dialect">${database.dialect}</prop>
        <prop key="hibernate.hbm2ddl.auto">update</prop>
        <prop key="hibernate.jdbc.batch_size">${database.batchSize}</prop>
      </props>
    </property>
</bean>

<bean id="jpaVendorAdapter"
      class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter"
      p:generateDdl="true"
      p:showSql="true" />

<bean id="`"
      class="org.jasig.cas.services.JpaServiceRegistryDaoImpl" />

<bean id="transactionManager"
      class="org.springframework.orm.jpa.JpaTransactionManager"
      p:entityManagerFactory-ref="factoryBean" />

<!--
   | Injects EntityManager/Factory instances into beans with
   | @PersistenceUnit and @PersistenceContext
   -->
<bean class="org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor" />
{% endhighlight %}

<a name="InstallingtheServicesManagementWebapp">  </a>
## Installing the Services Management Webapp

The services management webapp is no more part of the CAS server and is a standalone web application: `cas-management-webapp`.

Nonetheless, one must keep in mind that both applications (the CAS server and the services management webapp) share the _same_ configuration for the CAS services:
* the management webapp is used to add/edit/delete all the CAS services
* the CAS server loads/relies on all these defined CAS services to process all incoming requests.

You can install the services management webapp in your favourite applications server, there is no restriction.
Though, you need at first to configure it according to your environment. Towards that goal, the best way to proceed is to create your own services management webapp using a [Maven overlay](http://maven.apache.org/plugins/maven-war-plugin/overlays.html) based on the CAS services management webapp:

{% highlight xml %}
<dependency>
  <groupId>org.jasig.cas</groupId>
  <artifactId>cas-management-webapp</artifactId>
  <version>${cas.version}</version>
  <type>war</type>
  <scope>runtime</scope>
</dependency>
{% endhighlight %}

<a name="Authenticationmethod">  </a>
### Authentication method

By default, the `cas-management-webapp` is configured to authenticate against a CAS server. We assume that it's the case in this documentation. However, you could change the authentication method by overriding the `WEB-INF/spring-configuration/securityContext.xml` file.

<a name="Urlsconfiguration">  </a>
### Urls configuration

The urls configuration of the CAS server and management applications can be done by overriding the default `WEB-INF/cas-management.properties` file:

    # CAS
    cas.host=http://localhost:8080
    cas.prefix=${cas.host}/cas
    cas.securityContext.casProcessingFilterEntryPoint.loginUrl=${cas.prefix}/login
    cas.securityContext.ticketValidator.casServerUrlPrefix=${cas.prefix}
    # Management
    cas-management.host=${cas.host}
    cas-management.prefix=${cas-management.host}/cas-management
    cas-management.securityContext.serviceProperties.service=${cas-management.prefix}/j_spring_cas_security_check
    cas-management.securityContext.serviceProperties.adminRoles=ROLE_ADMIN

When authenticating against a CAS server, the services management webapp will be processed as a regular CAS service and thus, needs to be defined in the services registry (of the CAS server).

<a name="Servicesregistry">  </a>
### Services registry

You also need to define the *common* services registry by overriding the `WEB-INF/managementConfigContext.xml` file and set the appropriate `serviceRegistryDao` (see above: *Persisting Registered Service Data*). It should be the same configuration you already use in your CAS server (in the `WEB-INF/deployerConfigContext.xml` file).

<a name="UI">  </a>
### UI

The services management webapp is pretty simple to use:
* use the "Manage Services" link to see the list of all CAS services
* click the "Add New Service" link to add a new CAS service
* click the "edit" link with the pen image (on the right of a CAS service definition) to edit a specific CAS service
* click the "delete" link with the trash image (on the right of a CAS service definition) to delete a specific CAS service (after a confirmation alert).

