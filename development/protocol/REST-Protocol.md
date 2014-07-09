---
layout: default
title: CAS - CAS REST Protocol
---

#REST Protocol
The REST protocol allows one to model applications as users, programmatically acquiring service tickets to authenticate to other applications. This means that other applications would be able to use a CAS client  to accept Service Tickets rather than to rely upon another technology such as client SSL certificates for application-to-application authentication of requests. This is achieved by exposing a way to RESTfully obtain a Ticket Granting Ticket and then use that to obtain a Service Ticket.

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>The REST endpoint may become a tremendously convenient target for brute force dictionary attacks on CAS server. Enable support only soberly and with due consideration of security aspects.</p></div>


#Components
By default the CAS RESTful API is configured in the `restlet-servlet.xml`, which contains the routing for the tickets. It also defines the resources that will resolve the URLs. The `TicketResource` defined by default (which can be extended) accepts username/password.

Support is enabled by including the following in your `pom.xml` file:

    <dependency>
        <groupId>org.jasig.cas</groupId>
        <artifactId>cas-server-integration-restlet</artifactId>
        <version>${cas.version}</version>
    </dependency>

REST support is currently provided internally by the [Restlet framework](http://restlet.org/‎).


#Configuration
To turn on the protocol, add the following to the `web.xml`:
{% highlight xml %}
<servlet>
    <servlet-name>restlet</servlet-name>
    <servlet-class>org.restlet.ext.spring.RestletFrameworkServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>
 
<servlet-mapping>
    <servlet-name>restlet</servlet-name>
    <url-pattern>/v1/*</url-pattern>
</servlet-mapping>
{% endhighlight %}


#Protocol


##Request a Ticket Granting Ticket


###Sample Request
{% highlight bash %}
POST /cas/v1/tickets HTTP/1.0
 
username=battags&password=password&additionalParam1=paramvalue
{% endhighlight %}


###Sample Response


####Successful Response
{% highlight bash %}
201 Created
Location: http://www.whatever.com/cas/v1/tickets/{TGT id}
{% endhighlight %}


####Unsuccessful Response
If incorrect credentials are sent, CAS will respond with a 400 Bad Request error (will also respond for missing parameters, etc.). If you send a media type it does not understand, it will send the 415 Unsupported Media Type.


##Request a Service Ticket

###Sample Request
{% highlight bash %}
POST /cas/v1/tickets/{TGT id} HTTP/1.0
 
service={form encoded parameter for the service url}
{% endhighlight %}

###Sample Response

####Successful Response
{% highlight bash %}
200 OK
ST-1-FFDFHDSJKHSDFJKSDHFJKRUEYREWUIFSD2132
{% endhighlight %}
####Unsuccessful Response
CAS will send a 400 Bad Request. If an incorrect media type is sent, it will send the 415 Unsupported Media Type.


##Logout
{% highlight bash %}
DELETE /cas/v1/tickets/TGT-fdsjfsdfjkalfewrihfdhfaie HTTP/1.0
{% endhighlight %}

#Using cas-server-support-rest module
Version 4.1.0 includes a new maven module namely cas-server-support-rest which implements REST API with a modern
style programming model and eliminates an extra dependency on the restlet library. The public REST API is not
changed in this module and switching to it is just a matter of a few minor local war overlay changes.

For deployments that do not use restlet module, the enablement of the REST support is as easy as including this dependency in the overlay's pom:

{% highlight xml %}
<dependency>
   <groupId>org.jasig.cas</groupId>
   <artifactId>cas-server-extension-rest</artifactId>
   <version>${cas.version}</version>
   <scope>runtime</scope>
</dependency>
{% endhighlight %}

For users of restlet module, just replace the pom dependency and change this snippet in web.xml:

from this:

{% highlight xml %}
<servlet>
    <servlet-name>restlet</servlet-name>
    <servlet-class>org.restlet.ext.spring.RestletFrameworkServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>
 
<servlet-mapping>
    <servlet-name>restlet</servlet-name>
    <url-pattern>/v1/*</url-pattern>
</servlet-mapping>
{% endhighlight %}

to this:

{% highlight xml %}
    <servlet-mapping>
        <servlet-name>cas</servlet-name>
        <url-pattern>/v1/*</url-pattern>
    </servlet-mapping>
{% endhighlight %}

or delete the web.xml in the overlay altogether if there are no other customizations there as this mapping is provided by CAS' webapp module's web.xml out of the box.

Please note, if there are local customization in overlay's web.xml, the following `contextConfigLocation` contex param must also be added in order to enable new rest module: `classpath*:/META-INF/spring/*.xml` So the entire context-param block would look like this:

{% highlight xml %}
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>
      /WEB-INF/spring-configuration/*.xml
      /WEB-INF/deployerConfigContext.xml
      classpath*:/META-INF/spring/*.xml
    </param-value>
</context-param>
{% endhighlight %}
