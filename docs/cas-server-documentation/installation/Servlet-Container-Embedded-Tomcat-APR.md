---
layout: default
title: CAS - Servlet Container
category: Installation
---
{% include variables.html %}

# Apache Tomcat - Embedded Servlet Container APR

Apache Tomcat can use the [Apache Portable Runtime](https://tomcat.apache.org/tomcat-11.0-doc/apr.html) to provide superior
scalability, performance, and better integration with native server technologies.

Enabling APR requires the following JVM system property that indicates
the location of the APR library binaries (i.e. `usr/local/opt/tomcat-native/lib`):

```bash
-Djava.library.path=/path/to/tomcat-native/lib
```
