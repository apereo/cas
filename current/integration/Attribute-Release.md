---
layout: default
title: CAS - Attribute Release
---

# Attribute Release
Attributes are controlled by the [Person Directory project](https://github.com/Jasig/person-directory‎) and returned to scoped services via the [SAML 1.1 protocol](../protocol/SAML-Protocol.html) or the [CAS protocol](../protocol/CAS-Protocol.html). The Person Directory dependency is automatically bundled with the CAS server. Therefor, declaring an additional dependency will not be required. This Person Directory project supports both LDAP and JDBC attribute release, caching, attribute aggregation from multiple attribute sources, etc.

Attributes pass through a two-step process:
* Resolution: Done at the time of establishing the principal via `PrincipalResolver` components where attributes are resolved from various sources that are outlined below.
* Release: Adopters must explicitly configure attribute release for services in order for the resolved attributes to be released to a service in the validation response. 


## Components
A PersonDirectory `IPersonAttributeDao` attribute source is defined and configured to describe the global set of attributes to be fetched for each authenticated principal. That global set of attributes is then filtered by the service manager according to service-specific attribute release rules. 


### Person Directory
* `MergingPersonAttributeDaoImpl`: Designed to query multiple `IPersonAttributeDaos` in order and merge the results into a single result set. Merging strategies may be configured via instances of `IAttributeMerger`.
* `CachingPersonAttributeDaoImpl`: Provides the ability to cache results of executed inner DAOs.
* `CascadingPersonAttributeDao`: Designed to query multiple `IPersonAttributeDaos` in order and merge the results into a single result set. As each `IPersonAttributesAttributeDao` is queried the attributes from the first `IPersonAttributes` in the result set are used as the query for the next `IPersonAttributesAttributeDao`. 
* `StubPersonAttributeDao`: Backed by a single Map which this implementation will always return, useful for returning static values.
* `MessageFormatPersonAttributeDao`: Provides the ability to create attributes based on other other attribute values as arguments.
* `RegexGatewayPersonAttributeDao`: Conditionally execute an inner DAO if the data in the seed matches criteria set out by the configured patterns.
* `SingleRowJdbcPersonAttributeDao`: The implementation that maps from column names in the result of a SQL query to attribute names.
* `MultiRowJdbcPersonAttributeDao`: Designed to work against a table where there is a mapping of one row to many users. Should be used if the database is structured such that there is a column for attribute names and column(s) for the corresponding values.
* `XmlPersonAttributeDao`: XML backed person attribute DAO that supports wildcard searching.
* `LdapPersonAttributeDao`: Queries an LDAP directory to populate person attributes using Spring Framework.

More about the Person Directory and its configurable sources [can be found here](https://wiki.jasig.org/display/PDM15/Person+Directory+1.5+Manual).


### CAS
The CAS project provides the following additional implementations:

* `LdapPersonAttributeDao`: Queries an LDAP directory to populate person attributes using the bundled CAS LDAP libraries.


### Sample Usage


####LDAP
The following snippet assumes that connection information beans are already defined.

{% highlight xml %}
<bean id="ldapPersonAttributeDao"
      class="org.jasig.cas.persondir.LdapPersonAttributeDao"
      p:connectionFactory-ref="pooledLdapConnectionFactory"
      p:baseDN="${ldap.baseDn}"
      p:searchControls-ref="searchControls"
      p:searchFilter="mail={0}">
    <property name="resultAttributeMapping">
        <map>
            <!--
               | Key is LDAP attribute name, value is principal attribute name.
               -->
            <entry key="member" value="member" />
            <entry key="mail" value="mail" />
            <entry key="displayName" value="displayName" />
        </map>
    </property>
</bean>
{% endhighlight %}


####JDBC
The following snippet assumes that connection information beans are already defined.

{% highlight xml %}
<bean id="singleRowJdbcPersonAttributeDao"
    class="org.jasig.services.persondir.support.jdbc.SingleRowJdbcPersonAttributeDao">
    <constructor-arg index="0" ref="dataSource" />
    <constructor-arg index="1" value="SELECT * FROM USER_DATA WHERE {0}" />
    <property name="queryAttributeMapping">
        <map>
            <entry key="username" value="uid" />
        </map>
    </property>
    <property name="resultAttributeMapping">
        <map>
            <entry key="uid" value="username" />
            <entry key="first_name" value="first_name" />
            <entry key="last_name" value="last_name" />
            <entry key="email" value="email" />
        </map>
    </property>
</bean>
{% endhighlight %}


####Caching, Merging and Cascading
{% highlight xml %}
<bean id="mergedPersonAttributeDao"
        class="org.jasig.services.persondir.support.CachingPersonAttributeDaoImpl">
    <property name="cacheNullResults" value="true" />
    <property name="userInfoCache">
        <bean class="org.jasig.portal.utils.cache.MapCacheFactoryBean">
            <property name="cacheFactory" ref="cacheFactory" />
            <property name="cacheName" value="org.jasig.services.persondir.USER_INFO.merged" />
        </bean>
    </property>
    <property name="cachedPersonAttributesDao" >
        <bean id="mergedPersonAttributeDao"                 
                class="org.jasig.services.persondir.support.MergingPersonAttributeDaoImpl">
            <property name="merger">
                <bean class="org.jasig.services.persondir.support.merger.NoncollidingAttributeAdder" />
            </property>
            <property name="personAttributeDaos">
                <list>
                    <bean class="org.jasig.services.persondir.support.CascadingPersonAttributeDao">
                        <property name="personAttributeDaos">
                            <list>
                                <ref bean="anotherDao" />
                            </list>
                        </property>
                    </bean>
                </list>
            </property>
        </bean>
    </property>
</bean>
{% endhighlight %}


## Configuration
Once principal attributes are resolved, adopters may choose to allow/release each attribute per each definition in the registry. Example configuration follows:

{% highlight xml %}
<bean class="org.jasig.cas.services.RegisteredServiceImpl">
  <property name="id" value="0" />
  <property name="name" value="HTTPS Services" />
  <property name="description" value="YOUR HTTP Service" />
  <property name="serviceId" value="https://**" />
  <property name="allowedAttributes">
    <list>
      <value>yourAttributeName</value>
    </list>              
  </property>
</bean>
{% endhighlight %}


### Principal-Id Attribute
The service registry component of CAS has the ability to allow for configuration of a `usernameAttribute` to be returned for the given registered service. When this property is set for a service, CAS will return the value of the configured attribute as part of its validation process. 

* Ensure the attribute is available and resolved for the principal
* Specify the attribute in its list of allowed attributes
* Set the `usernameAttribute` property of the given service to the attribute you defined

{% highlight xml %}
<bean class="org.jasig.cas.services.RegisteredServiceImpl">
  <property name="id" value="0" />
  <property name="name" value="HTTPS Services" />
  <property name="description" value="YOUR HTTPS Service" />
  <property name="serviceId" value="https://**" />
  <property name="evaluationOrder" value="0" />
  <property name="usernameAttribute" value="mail" />
  <property name="allowedAttributes">
    <list>
      <value>someAttributeName</value>
    </list>              
  </property>
</bean>
{% endhighlight %}


### Attribute Filters
The service registry component has the ability to allow for configuration of an attribute filter. Filters have the ability to do execute additional processes on the set of attributes that are allocated to the final principal for a given user id. For instance, you might want to decide that certain attribute need to be removed from the final resultset based on a user role, etc. 


####`RegisteredServiceAttributeFilter`
If you wish to write your own filter, you need to design a class that implements this interface and as such, plug that custom instance into the specific service registry entry you intend to use. 


####`RegisteredServiceRegexAttributeFilter`
A regex-aware filter responsible to make sure only attributes that match a certain  pattern are released for a given service. 

This example also demonstrates the configuration of an attribute filter that only allows for attributes whose length is 3.
 
{% highlight xml %}
<bean class="org.jasig.cas.services.RegexRegisteredService">
   <property name="id" value="1" />
   <property name="name" value="HTTP and IMAP on example.com" />
   <property name="description" value="Allows HTTP(S) and IMAP(S) protocols on example.com" />
   <property name="serviceId" value="^(https?|imaps?)://([A-Za-z0-9_-]+\.)*example\.com/.*" />
   <property name="evaluationOrder" value="0" />
   <property name="attributeFilter">
      <bean class="org.jasig.cas.services.support.RegisteredServiceRegexAttributeFilter" 
            c:regex="^\w{3}$" /> 
   </property>
</bean>
{% endhighlight %}



