---
layout: default
title: CAS - Servlet Container
category: Installation
---
{% include variables.html %}

# Apache Tomcat - Embedded Servlet Container Logging

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

