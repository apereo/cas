---
layout: default
title: CAS - Servlet Container
category: Installation
---
{% include variables.html %}

# Apache Tomcat - Embedded Servlet Container Configuration

Note that by default, the embedded container attempts to enable the HTTP2 protocol.

{% include casmodule.html group="org.apereo.cas" module="cas-server-webapp-tomcat" %}

## IPv4 Configuration

In order to force Apache Tomcat to use IPv4, configure the following as a system property for your *run* command:

```bash
-Djava.net.preferIPv4Stack=true 
```

The same sort of configuration needs to be applied to your `$CATALINA_OPTS` 
environment variable in case of an external container.

## Faster Startup

[This guide](https://cwiki.apache.org/confluence/display/TOMCAT/HowTo+FasterStartUp) provides 
several recommendations on how to make web applications and Apache Tomcat as a whole to start up faster.

## Logging

The embedded Apache Tomcat container is presently unable to display any log messages below `INFO` even if your CAS log 
configuration explicitly asks for `DEBUG` or `TRACE` level data. 
See [this bug report](https://github.com/spring-projects/spring-boot/issues/2923) to learn more.

While workarounds and fixes may become available in the future, for the time being, you may execute the following 
changes to get `DEBUG` level log data from the embedded Apache Tomcat. This 
is specially useful if you are troubleshooting the behavior 
of Tomcat's internal components such as valves, etc.

- Design a `logging.properties` file as such:

```properties
handlers = java.util.logging.ConsoleHandler
.level = ALL
java.util.logging.ConsoleHandler.level = FINER
java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter
```

- Design a`java.util.logging.config.file` setting as a system/environment variable or command-line 
argument whose value is set to the `logging.properties` path. Use the setting when you launch and deploy CAS.

For instance:

```bash
java -jar /path/to/cas.war -Djava.util.logging.config.file=/path/to/logging.properties
```

### Configuration

{% include casproperties.html thirdPartyStartsWith="server.tomcat." %}

#### HTTP Proxying

{% include casproperties.html properties="cas.server.tomcat.http-proxy." %}
                       

#### HTTP

{% include casproperties.html properties="cas.server.tomcat.http." %}
                

#### AJP


{% include casproperties.html properties="cas.server.tomcat.ajp." %}

#### SSL Valve

The Apache Tomcat `SSLValve` is a way to get a client certificate from an SSL proxy (e.g. HAProxy or BigIP F5)
running in front of Tomcat via an HTTP header. If you enable this, make sure your proxy is ensuring
that this header does not originate with the client (e.g. the browser).

{% include casproperties.html properties="cas.server.tomcat.ssl-valve" %}

Example HAProxy Configuration (snippet): Configure SSL frontend
with cert optional, redirect to cas, if cert provided, put it on header.

```
frontend web-vip
  bind 192.168.2.10:443 ssl crt /var/lib/haproxy/certs/www.example.com.pem ca-file /var/lib/haproxy/certs/ca.pem verify optional
  mode http
  acl www-cert ssl_fc_sni if { www.example.com }
  acl empty-path path /
  http-request redirect location /cas/ if empty-path www-cert
  http-request del-header ssl_client_cert unless { ssl_fc_has_crt }
  http-request set-header ssl_client_cert -----BEGIN\ CERTIFICATE-----\ %[ssl_c_der,base64]\ -----END\ CERTIFICATE-----\  if { ssl_fc_has_crt }
  acl cas-path path_beg -i /cas
  reqadd X-Forwarded-Proto:\ https
  use_backend cas-pool if cas-path

backend cas-pool
  option httpclose
  option forwardfor
  cookie SERVERID-cas insert indirect nocache
  server cas-1 192.168.2.10:8080 check cookie cas-1
```

#### Extended Access Log Valve

{% include casproperties.html properties="cas.server.tomcat.ext-access-log." %}

#### Rewrite Valve

{% include casproperties.html properties="cas.server.tomcat.rewrite-valve." %}

#### Basic Authentication

{% include casproperties.html properties="cas.server.tomcat.basic-authn." %}

#### Apache Portable Runtime (APR)

Apache Tomcat can use the [Apache Portable Runtime](https://tomcat.apache.org/tomcat-9.0-doc/apr.html) to provide superior
scalability, performance, and better integration with native server technologies.

{% include casproperties.html properties="cas.server.tomcat.apr" %}

Enabling APR requires the following JVM system property that indicates
the location of the APR library binaries (i.e. `usr/local/opt/tomcat-native/lib`):

```bash
-Djava.library.path=/path/to/tomcat-native/lib
```

#### Connector IO
                      

{% include casproperties.html properties="cas.server.tomcat.socket." %}
                                  

#### Session Clustering & Replication

Enable in-memory session replication to replicate web application session deltas.

| Clustering Type      | Description
|----------------------|-------------------------------------------------------
| `DEFAULT`            | Discovers cluster members via multicast discovery and optionally via staticly defined cluster members using the `clusterMembers`. [SimpleTcpCluster with McastService](http://tomcat.apache.org/tomcat-9.0-doc/cluster-howto.html)
| `CLOUD`              | For use in Kubernetes where members are discovered via accessing the Kubernetes API or doing a DNS lookup of the members of a Kubernetes service. [Documentation](https://cwiki.apache.org/confluence/display/TOMCAT/ClusteringCloud) is currently light, see code for details.

| Membership Providers   | Description
|----------------------|-------------------------------------------------------
| `kubernetes`         | Uses [Kubernetes API](https://github.com/apache/tomcat/blob/master/java/org/apache/catalina/tribes/membership/cloud/KubernetesMembershipProvider.java) to find other pods in a deployment. API is discovered and accessed via information in environment variables set in the container. The KUBERNETES_NAMESPACE environment variable is used to query the pods in the namespace and it will treat other pods in that namespace as potential cluster members but they can be filtered using the KUBERNETES_LABELS environment variable which are used as a [label selector](https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/#api).
| `dns`                | Uses [DNS lookups](https://github.com/apache/tomcat/blob/master/java/org/apache/catalina/tribes/membership/cloud/DNSMembershipProvider.java) to find addresses of the cluster members behind a DNS name specified by DNS_MEMBERSHIP_SERVICE_NAME environment variable. Works in Kubernetes but doesn't rely on Kubernetes.
| `MembershipProvider` class | Use a [membership provider implementation](https://github.com/apache/tomcat/blob/master/java/org/apache/catalina/tribes/MembershipProvider.java) of your choice.

Most settings apply to the `DEFAULT` clustering type, which requires members to be defined via `clusterMembers` if multicast discovery doesn't work. The `cloudMembershipProvider` setting applies to the `CLOUD` type.

{% include casproperties.html properties="cas.server.tomcat.clustering." %}


