---
layout: default
title: CAS - Fortress Authentication
---

# Apache Fortress Authentication

Services connected to CAS can also use Apache Fortress to handle the authentication and authorization with fortress. The Idea of this is because Apache Fortress doesn't have any SSO mechanism. However, Apache Fortress is complied for ANSI INCITS 359 RBAC.  
[Here](http://directory.apache.org/fortress/testimonials.html) is the link how it was worked before i submit this feature for cas.

To enable this support's feature, make sure Apache Fortress have been  [installed](http://directory.apache.org/fortress/installation.html "apache fortress installation") and include this dependency in your war overlay project:  
```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-fortress</artifactId>
    <version>${cas.version}</version>
</dependency>
```  

For this current version, Fortress will only support using tomcat as the web container. However, we will try to support other web container as well in the future.

## Server side configuration

### Configure Apache Fortress Properties
Configure `fortress.properties` file and put it under your $TOMCAT_HOME/lib or you can append your own classpath configuration.  

### Fortress Realm Proxy
Put Fortress Realm Proxy under your $TOMCAT_HOME/lib

### Fortress Version
Add `-Dversion=<your.fortress.version>` to `JAVA_OPTS` or `CATALINA_OPTS.  
It is also possible if you would like to add it to your overlay spring definition :
```xml
  <bean class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
    <property name="targetObject">
      <bean class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
        <property name="targetClass" value="java.lang.System" />
        <property name="targetMethod" value="getProperties" />
      </bean>
    </property>
    <property name="targetMethod" value="putAll" />
    <property name="arguments">
      <util:properties>
        <prop key="version">1.1.0</prop>
      </util:properties>
    </property>
  </bean>
```

## Client side configuration

### Fortress Session Attribute
This plugin will append new attribute with key `fortressSession` and as the client you need to extract this key in order to get [org.apache.directory.fortress.core.model.Session](http://directory.apache.org/fortress/gen-docs/latest/apidocs/org/apache/directory/fortress/core/model/Session.html) in xml form.
With Fortress session later you can get the roles or get the permission 
dynamically by calling fortress rest.