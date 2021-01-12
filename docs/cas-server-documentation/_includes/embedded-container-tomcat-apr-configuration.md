
Tomcat can use the [Apache Portable Runtime](https://tomcat.apache.org/tomcat-9.0-doc/apr.html) to provide superior
scalability, performance, and better integration with native server technologies.

```properties
# cas.server.tomcat.apr.enabled=false

# cas.server.tomcat.apr.ssl-protocol=
# cas.server.tomcat.apr.ssl-verify-depth=10
# cas.server.tomcat.apr.ssl-verify-client=require
# cas.server.tomcat.apr.ssl-cipher-suite=
# cas.server.tomcat.apr.ssl-disable-compression=false
# cas.server.tomcat.apr.ssl-honor-cipher-order=false

# cas.server.tomcat.apr.ssl-certificate-chain-file=
# cas.server.tomcat.apr.ssl-ca-certificate-file=
# cas.server.tomcat.apr.ssl-certificate-key-file=
# cas.server.tomcat.apr.ssl-certificate-file=
```

Enabling APR requires the following JVM system property that indicates
the location of the APR library binaries (i.e. `usr/local/opt/tomcat-native/lib`):

```bash
-Djava.library.path=/path/to/tomcat-native/lib
```

The APR connector can be assigned an SSLHostConfig element as such:

```properties
# cas.server.tomcat.apr.ssl-host-config.enabled=false
# cas.server.tomcat.apr.ssl-host-config.revocation-enabled=false
# cas.server.tomcat.apr.ssl-host-config.ca-certificate-file=false
# cas.server.tomcat.apr.ssl-host-config.host-name=
# cas.server.tomcat.apr.ssl-host-config.ssl-protocol=
# cas.server.tomcat.apr.ssl-host-config.protocols=all
# cas.server.tomcat.apr.ssl-host-config.insecure-renegotiation=false
# cas.server.tomcat.apr.ssl-host-config.certificate-verification-depth=10

# cas.server.tomcat.apr.ssl-host-config.certificates[0].certificate-file=
# cas.server.tomcat.apr.ssl-host-config.certificates[0].certificate-key-file=
# cas.server.tomcat.apr.ssl-host-config.certificates[0].certificate-key-password=
# cas.server.tomcat.apr.ssl-host-config.certificates[0].certificate-chain-file=
# cas.server.tomcat.apr.ssl-host-config.certificates[0].type=UNDEFINED
```
