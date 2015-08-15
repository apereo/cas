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
This DAO reads services definitions from JSON configuration files at the application context initialization time. JSON files are
expected to be found inside a configured directory location and this DAO will recursively look through the directory structure to find relevant JSON files.

{% highlight xml %}
<bean id="serviceRegistryDao" class="org.jasig.cas.services.JsonServiceRegistryDao"
          c:configDirectory="file:/etc/cas/json" />
{% endhighlight %}

A sample JSON file follows:

{% highlight json %}
{
    "@class" : "org.jasig.cas.services.RegexRegisteredService",
    "id" : 103935657744185,
    "description" : "Service description",
    "serviceId" : "https://**",
    "name" : "testJsonFile",
    "theme" : "testtheme",
    "proxyPolicy" : {
        "@class" : "org.jasig.cas.services.RegexMatchingRegisteredServiceProxyPolicy",
        "pattern" : "https://.+"
    },
    "accessStrategy" : {
        "@class" : "org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy"
    },
    "evaluationOrder" : 1000,
    "usernameAttributeProvider" : {
        "@class" : "org.jasig.cas.services.DefaultRegisteredServiceUsernameProvider"
    },
    "logoutType" : "BACK_CHANNEL",
    "requiredHandlers" : [ "java.util.HashSet", [ "handler1", "handler2" ] ],
    "attributeReleasePolicy" : {
        "@class" : "org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy",
        "attributeFilter" : {
            "@class" : "org.jasig.cas.services.support.RegisteredServiceRegexAttributeFilter",
            "pattern" : "\\w+"
        },
        "allowedAttributes" : [ "java.util.ArrayList", [ "uid", "cn", "sn" ] ]
    }
}

{% endhighlight %}


<div class="alert alert-warning"><strong>Clustering Services</strong><p>
You MUST consider that if your CAS server deployment is clustered, each CAS node in the cluster must have
access to the same set of JSON configuration files as the other, or you may have to devise a strategy to keep
changes synchronized from one node to the next.
</p></div>

The JSON service registry is also able to auto detect changes to the specified directory. It will monitor changes to recognize
file additions, removals and updates and will auto-refresh CAS so changes do happen instantly.

The naming convention for new JSON files is recommended to be the following:


{% highlight bash %}

JSON fileName = serviceName + "-" + serviceNumericId + ".json"

{% endhighlight %}


Based on the above formula, for example the above JSON snippet shall be named: `testJsonFile-103935657744185.json`

<div class="alert alert-warning"><strong>Duplicate Services</strong><p>
As you add more files to the directory, you need to be absolutely sure that no two service definitions
will have the same id. If this happens, loading one definition will stop loading the other. While service ids
can be chosen arbitrarily, make sure all service numeric identifiers are unique. CAS will also output warnings
if duplicate data is found.
</p></div>


######`MongoServiceRegistryDao`
This DAO uses a [MongoDb](https://www.mongodb.org/) instance to load and persist service definitions. Support is enabled by adding the following module into the Maven overlay:

{% highlight xml %}
<dependency>
    <groupId>org.jasig.cas</groupId>
    <artifactId>cas-server-integration-mongo</artifactId>
    <version>${cas.version}</version>
</dependency>
{% endhighlight %}

###### Configuration

This implementation auto-configures most of the internal details.
The following configuration in `cas.properties` is required.

{% highlight properties %}
mongodb.host=mongodb database url
mongodb.port=mongodb database port
mongodb.userId=mongodb userid to bind
mongodb.userPassword=mongodb password to bind
cas.service.registry.mongo.db=Collection name to store service definitions
{% endhighlight %}

######`LdapServiceRegistryDao`
Service registry implementation which stores the services in a LDAP Directory. Uses an instance of `LdapRegisteredServiceMapper`, that by default is `DefaultLdapRegisteredServiceMapper` in order to configure settings for retrieval, search and persistence of service definitions. By default, entries are assigned the `objectclass` `casRegisteredService` attribute and are looked up by the `uid` attribute.

{% highlight xml %}

<context:component-scan base-package="org.jasig.cas" />

<bean id="serviceRegistryDao"
      class="org.jasig.cas.adaptors.ldap.services.LdapServiceRegistryDao"
      p:connectionFactory-ref="pooledLdapConnectionFactory"
      p:searchRequest-ref="searchRequest"
      p:ldapServiceMapper-ref="ldapServiceMapper" />

<bean id="ldapServiceMapper"
      class="org.jasig.cas.adaptors.ldap.services.DefaultLdapRegisteredServiceMapper"/>
{% endhighlight %}

Note that the configuration of the mapper is optional and need not explicitly exist.

<p/>

######`DefaultLdapRegisteredServiceMapper`
The default mapper has support for the following optional items:

| Field                             | Default Value
|-----------------------------------+--------------------------------------------------+
| `objectClass`                     | casRegisteredService
| `serviceDefinitionAttribute`      | description
| `idAttribute`                     | uid

Service definitions are by default stored inside the `serviceDefinitionAttribute` attribute as JSON objects. The format and syntax of the JSON is identical to that of `JsonServiceRegistryDao`.


######`JpaServiceRegistryDaoImpl`
Stores registered service data in a database; the preferred choice when using the service management webapp.
The following schema shall be generated by CAS automatically for brand new deployments, and must be massaged
when doing CAS upgrades:

{% highlight sql %}

create table RegisteredServiceImpl (
    expression_type VARCHAR(15) DEFAULT 'ant' not null,
    id bigint generated by default as identity (start with 1),
    access_strategy blob(255),
    attribute_release blob(255),
    description varchar(255) not null,
    evaluation_order integer not null,
    logo varchar(255),
    logout_type integer,
    logout_url varchar(255),
    name varchar(255) not null,
    proxy_policy blob(255),
    required_handlers blob(255),
    public_key blob(255),
    serviceId varchar(255) not null,
    theme varchar(255),
    username_attr blob(255),
    primary key (id)
)

{% endhighlight %}


The following configuration template may be applied to `deployerConfigContext.xml` to provide for persistent
registered service storage. The configuration assumes a `dataSource` bean is defined in the context.

{% highlight xml %}
<tx:annotation-driven />

<bean id="factoryBean"
      class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean"
      p:dataSource-ref="dataSource"
      p:jpaVendorAdapter-ref="jpaVendorAdapter"
      p:packagesToScan-ref="packagesToScan">
    <property name="jpaProperties">
      <props>
        <prop key="hibernate.dialect">${database.hibernate.dialect}</prop>
        <prop key="hibernate.hbm2ddl.auto">update</prop>
        <prop key="hibernate.jdbc.batch_size">${database.hibernate.batchSize:10}</prop>
      </props>
    </property>
</bean>

<util:list id="packagesToScan">
    <value>org.jasig.cas.services</value>
    <value>org.jasig.cas.ticket</value>
    <value>org.jasig.cas.adaptors.jdbc</value>
</util:list>

<bean id="jpaVendorAdapter"
      class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter"
      p:generateDdl="true"
      p:showSql="true" />

<bean id="serviceRegistryDao"
      class="org.jasig.cas.services.JpaServiceRegistryDaoImpl" />

<bean id="transactionManager"
      class="org.springframework.orm.jpa.JpaTransactionManager"
      p:entityManagerFactory-ref="factoryBean" />

<!--
   | Injects EntityManager/Factory instances into beans with
   | @PersistenceUnit and @PersistenceContext
-->
<bean class="org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor" />

<!--
   Configuration via JNDI
-->
<bean id="dataSource" class="org.springframework.jndi.JndiObjectFactoryBean"
    p:jndiName="java:comp/env/jdbc/cas-source" />
{% endhighlight %}

If you prefer a direct connection to the database, here's a sample configuration of the `dataSource`:

{% highlight xml %}
 <bean
        id="dataSource"
        class="org.apache.commons.dbcp2.BasicDataSource"
        p:driverClassName="org.hsqldb.jdbcDriver"
        p:jdbcUrl-ref="database"
        p:password=""
        p:username="sa" />
{% endhighlight %}

The data source will need to be modified for your particular database (i.e. Oracle, MySQL, etc.), but the name `dataSource` should be preserved. Here is a MYSQL sample:

{% highlight xml %}
<bean
        id="dataSource"
        class="org.apache.commons.dbcp2.BasicDataSource"
        p:driverClassName="com.mysql.jdbc.Driver"
        p:url="jdbc:mysql://localhost:3306/test?autoReconnect=true"
        p:password=""
        p:username="sa" />
{% endhighlight %}

You will also need to change the property `hibernate.dialect` in adequacy with your database in `cas.properties` and `deployerConfigContext.xml`.

For example, for MYSQL the setting would be:

In `cas.properties`:

{% highlight bash %}
database.hibernate.dialect=org.hibernate.dialect.MySQLDialect
{% endhighlight %}

In `deployerConfigContext.xml`:

{% highlight xml %}
<prop key="hibernate.dialect">${database.hibernate.dialect}</prop>
{% endhighlight %}

You will also need to ensure that the xml configuration file contains the `tx` namespace:

{% highlight xml %}
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:p="http://www.springframework.org/schema/p"
       xsi:schemaLocation="
       http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx.xsd">
{% endhighlight %}

Finally, when adding a new source new dependencies may be required on Hibernate, `commons-dbcp2`. Be sure to add those to your `pom.xml`. Below is a sample configuration for MYSQL. Be sure to adjust the version elements for the appropriate version number.

{% highlight xml %}

<dependency>
  	<groupId>org.jasig.cas</groupId>
  	<artifactId>cas-server-support-jdbc</artifactId>
  	<version>${cas.version}</version>
  	<scope>runtime</scope>
</dependency>

<dependency>
  	<groupId>org.apache.commons</groupId>
  	<artifactId>commons-dbcp2</artifactId>
	<version>${commons.dbcp.version}</version>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>${hibernate.version}</version>
    <scope>compile</scope>
</dependency>

<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-entitymanager</artifactId>
    <version>${hibernate.entitymgmr.version}</version>
</dependency>

<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>${mysql.connector.version}</version>
</dependency>

{% endhighlight %}

## Service Management Webapp
The Services Management web application is a standalone application that helps one manage service registrations and entries via a customizable user interface. The management web application *MUST* share the same registry configuration as
the CAS server itself so the entire system can load the same services data. To learn more about the management webapp,
[please see this guide](Installing-ServicesMgmt-Webapp.html).
