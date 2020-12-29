In the event that you decide to run CAS without any SSL configuration in 
the embedded Tomcat container and on a non-secure port yet wish to customize 
the connector configuration that 
is linked to the running port (i.e. `8080`), the following settings may apply:

```properties
# cas.server.tomcat.http-proxy.enabled=true
# cas.server.tomcat.http-proxy.secure=true
# cas.server.tomcat.http-proxy.protocol=AJP/1.3
# cas.server.tomcat.http-proxy.scheme=https
# cas.server.tomcat.http-proxy.redirect-port=
# cas.server.tomcat.http-proxy.proxy-port=
# cas.server.tomcat.http-proxy.attributes.attribute-name=attributeValue
```
