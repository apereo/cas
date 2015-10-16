---
layout: default
title: CAS - Services Management Webapp
---
# Services Management Webapp

The services management webapp is no longer part of the CAS server and is a standalone web application: `cas-management-webapp`.

Nonetheless, one MUST keep in mind that both applications (the CAS server and the services management webapp) 
share the _same_ configuration for the CAS services:

* The management webapp is used to add/edit/delete all the CAS services
* The CAS server loads/relies on all these defined CAS services to process all incoming requests.

You can install the services management webapp in your favorite applications server, there is no restriction.
Though, you need to configure it according to your environment. Towards that goal, the best way to 
proceed is to create your own services management webapp using 
a [Maven overlay](http://maven.apache.org/plugins/maven-war-plugin/overlays.html) based on the CAS services management webapp:

{% highlight xml %}
<dependency>
  <groupId>org.jasig.cas</groupId>
  <artifactId>cas-management-webapp</artifactId>
  <version>${cas.version}</version>
  <type>war</type>
  <scope>runtime</scope>
</dependency>
{% endhighlight %}


## Authentication method

By default, the `cas-management-webapp` is configured to authenticate against a CAS server. We assume that it's the case in this documentation. However, you could change the authentication method by overriding the `WEB-INF/spring-configuration/securityContext.xml` file.


##Securing Access and Authorization
Access to the management webapp is controlled via Spring Security. Rules are defined in the `/cas-management-webapp/src/main/webapp/WEB-INF/managementConfigContext.xml` file.


###Static List of Users
By default, access is limited to a static list of users whose credentials may be specified in a `user-details.properties` file that should be available on the runtime classpath. 

{% highlight xml %}
<sec:user-service id="userDetailsService" 
   properties="${user.details.file.location:classpath:user-details.properties}" />
{% endhighlight %}

You can change the location of this file, by uncommenting the following key in your `cas-management.properties` file:

{% highlight bash %}
##
# User details file location that contains list of users
# who are allowed access to the management webapp:
# 
# user.details.file.location = classpath:user-details.properties
{% endhighlight %}

The format of the file should be as such:

{% highlight bash %}
# The syntax of each entry should be in the form of:
# 
# username=password,grantedAuthority[,grantedAuthority][,enabled|disabled]

# Example:
# casuser=notused,ROLE_ADMIN
{% endhighlight %}


###LDAP-managed List of Users
If you wish allow access to the services management application via an LDAP group/server, open up the `deployerConfigContext` file of the management web application and adjust for the following:

{% highlight xml %}
<sec:ldap-server id="ldapServer" url="ldap://myserver:13060/"
                 manager-dn="cn=adminusername,cn=Users,dc=london-scottish,dc=com"
                 manager-password="mypassword" />
<sec:ldap-user-service id="userDetailsService" server-ref="ldapServer"
            group-search-base="cn=Groups,dc=mycompany,dc=com" group-role-attribute="cn"
            group-search-filter="(uniquemember={0})"
            user-search-base="cn=Users,dc=mycompany,dc=com"
            user-search-filter="(uid={0})"/>
{% endhighlight %}

You will also need to ensure that the `spring-security-ldap` dependency is available to your build at runtime:

{% highlight xml %}
<dependency>
   <groupId>org.springframework.security</groupId>
   <artifactId>spring-security-ldap</artifactId>
   <version>${spring.security.ldap.version}</version>
   <exclusions>
     <exclusion>
             <groupId>org.springframework</groupId>
             <artifactId>spring-aop</artifactId>
     </exclusion>
     <exclusion>
             <groupId>org.springframework</groupId>
             <artifactId>spring-tx</artifactId>
     </exclusion>
     <exclusion>
             <groupId>org.springframework</groupId>
             <artifactId>spring-beans</artifactId>
     </exclusion>
     <exclusion>
             <groupId>org.springframework</groupId>
             <artifactId>spring-context</artifactId>
     </exclusion>
     <exclusion>
             <groupId>org.springframework</groupId>
             <artifactId>spring-core</artifactId>
     </exclusion>
   </exclusions>
</dependency>
{% endhighlight %}


## Urls Configuration

The urls configuration of the CAS server and management applications can be done 
by overriding the default `WEB-INF/cas-management.properties` file:

{% highlight properties %}
# CAS
cas.host=http://localhost:8080
cas.prefix=${cas.host}/cas
cas.securityContext.casProcessingFilterEntryPoint.loginUrl=${cas.prefix}/login
cas.securityContext.ticketValidator.casServerUrlPrefix=${cas.prefix}

# Management
cas-management.host=${cas.host}
cas-management.prefix=${cas-management.host}/cas-management
cas-management.securityContext.serviceProperties.service=${cas-management.prefix}/login/cas
cas-management.securityContext.serviceProperties.adminRoles=hasRole('ROLE_ADMIN')
{% endhighlight %}

When authenticating against a CAS server, the services management webapp will be processed as a 
regular CAS service and thus, needs to be defined in the services registry of the CAS server.

## Services Registry

You also need to define the *common* services registry by overriding the `WEB-INF/managementConfigContext.xml` 
file and set the appropriate `serviceRegistryDao`. The [persistence storage](Service-Management.html) MUST be the same.
It should be the same configuration you already use in your CAS server in the `WEB-INF/deployerConfigContext.xml` file.

