Enable the [extended access log](https://tomcat.apache.org/tomcat-9.0-doc/config/valve.html#Extended_Access_Log_Valve)
for the embedded Apache Tomcat container.

```properties
# cas.server.tomcat.ext-access-log.enabled=false
# cas.server.tomcat.ext-access-log.pattern=c-ip s-ip cs-uri sc-status time x-threadname x-H(secure) x-H(remoteUser)
# cas.server.tomcat.ext-access-log.suffix=.log
# cas.server.tomcat.ext-access-log.prefix=localhost_access_extended
# cas.server.tomcat.ext-access-log.directory=
```
