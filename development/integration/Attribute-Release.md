---
layout: default
title: CAS - Attribute Release
---

# Attribute Release
Attributes are controlled by the [Person Directory project](https://github.com/Jasig/person-directory‎) and returned to scoped services via the [SAML 1.1 protocol](../protocol/SAML-Protocol.html) or the [CAS protocol](../protocol/CAS-Protocol.html). The Person Directory dependency is automatically bundled with the CAS server. Therefor, declaring an additional dependency will not be required. This Person Directory project supports both LDAP and JDBC attribute release, caching, attribute aggregation from multiple attribute sources, etc.

Attributes pass through a two-step process:

* Resolution: Done at the time of establishing the principal via `PrincipalResolver` components where attributes are resolved from various sources that are outlined below.
* Release: Adopters must explicitly configure attribute release for services in order for the resolved attributes to be released to a service in the validation response. 


<div class="alert alert-info"><strong>Service Management</strong><p>Attribute release may also be configured via the
<a href="../installation/Service-Management.html">Service Management tool</a>.</p></div>

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
* `GroovyPersonAttributeDao`: Resolve attributes based on an external groovy script.
* `TomlLdapPersonAttributeDao`: Resolve person attributes and insert the ldap/context settings from an external Toml file. 
* `JsonBackedComplexStubPersonAttributeDao`: Resolve person attributes that are specified in an external JSON file.

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
  <property name="attributeReleasePolicy">
    <bean class="org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy">
        <property name="allowedAttributes">
            <list>
                <value>uid</value>
                <value>groupMembership</value>
                <value>memberOf</value>
            </list>
        </property>
    </bean>
  </property>
</bean>
{% endhighlight %}


### Principal-Id Attribute
The service registry component of CAS has the ability to allow for configuration of a `usernameAttribute` to be returned for the given registered service. When this property is set for a service, CAS will return the value of the configured attribute as part of its validation process. 

* Ensure the attribute is available and resolved for the principal
* Set the `usernameAttribute` property of the given service to the attribute you defined

{% highlight xml %}
<bean class="org.jasig.cas.services.RegisteredServiceImpl">
  <property name="id" value="0" />
  <property name="name" value="HTTPS Services" />
  <property name="description" value="YOUR HTTPS Service" />
  <property name="serviceId" value="https://**" />
  <property name="evaluationOrder" value="0" />
  <property name="usernameAttribute" value="mail" />
</bean>
{% endhighlight %}


### Attribute Release Policy
The release policy decides how attributes are to be released for a given service. Each policy has the ability to apply an optional filter.

#### Components

#####`ReturnAllAttributeReleasePolicy`
Return all resolved attributes to the service. 

#####`ReturnAllAttributeReleasePolicy`
Return all resolved attributes to the service. 

<bean class="org.jasig.cas.services.RegisteredServiceImpl">
  ...
  <property name="attributeReleasePolicy">
    <bean class="org.jasig.cas.services.ReturnAllAttributeReleasePolicy" />
  </property>
</bean>

#####`ReturnAllowedAttributeReleasePolicy`
Only return the attributes that are explicitly allowed by the configuration. 

{% highlight xml %}
<bean class="org.jasig.cas.services.RegisteredServiceImpl">
  ...
  <property name="attributeReleasePolicy">
    <bean class="org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy">
        <property name="allowedAttributes">
            <list>
                <value>uid</value>
                <value>groupMembership</value>
                <value>memberOf</value>
            </list>
        </property>
    </bean>
  </property>
</bean>
{% endhighlight %}


#####`ReturnMappedAttributeReleasePolicy`
Similar to above, this policy will return a collection of allowed attributes for the service, but also allows those attributes to be mapped and "renamed" at the more granular service level.

For example, the following configuration will recognize the resolved attributes `uid`, `eduPersonAffiliation` and `groupMembership` and will then release `uid`, `affiliation` and `group` to the web application configured.

{% highlight xml %}
<bean class="org.jasig.cas.services.RegisteredServiceImpl">
  ...
  <property name="attributeReleasePolicy">
    <bean class="org.jasig.cas.services.ReturnMappedAttributeReleasePolicy">
        <property name="allowedAttributes" ref="allowedAttributesMap" />
    </bean>
  </property>
</bean>

<util:map id="allowedAttributesMap">
    <entry key="uid" value="uid" />
    <entry key="eduPersonAffiliation" value="affiliation" /> 
    <entry key="groupMembership" value="group" />
</util:map>
{% endhighlight %}


#### Attribute Filters
While each policy defines what attributes may be allowed for a given service, there are optional attribute filters that can be set per policy to further weed out attributes based on their **values**. 

##### Components

######`RegisteredServiceRegexAttributeFilter`
The regex filter that is responsible to make sure only attributes whose value matches a certain regex pattern are released.

Suppose that the following attributes are resolved:

| Name         					| Value 
| :--------------------------------	|:-------------
| `uid`        						| jsmith
| `groupMembership`        			| std  
| `cn`        						| JohnSmith   

The following configuration for instance considers the initial list of `uid`, `groupMembership` and then only allows and releases attributes whose value's length is 3 characters. Therefor, out of the above list, only `groupMembership` is released to the application.

{% highlight xml %}
<bean class="org.jasig.cas.services.RegexRegisteredService"
      p:id="10000001" p:name="HTTP and IMAP" p:description="Allows HTTP(S) and IMAP(S) protocols"
      p:serviceId="^(https?|imaps?)://.*" p:evaluationOrder="10000001">
    <property name="attributeReleasePolicy">
        <bean class="org.jasig.cas.services.ReturnAllowedAttributeReleasePolicy">
            <property name="allowedAttributes">
                <list>
                    <value>uid</value>
                    <value>groupMembership</value>
                </list>
            </property>
            <property name="attributeFilter">
                <bean class="org.jasig.cas.services.support.RegisteredServiceRegexAttributeFilter" c:regex="^\w{3}$" /> 
            </property>
        </bean>
    </property>
</bean>
{% endhighlight %}