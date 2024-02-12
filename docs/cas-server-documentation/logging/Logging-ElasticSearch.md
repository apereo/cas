---
layout: default
title: CAS - Google Cloud Logging Configuration
category: Logs & Audits
---

{% include variables.html %}

# Elastic Search

`JsonTemplateLayout` is a customizable, efficient, and garbage-free JSON generating layout. 
It encodes `LogEvent`s according to the structure described by the JSON template provided.

To format logs according to the Elastic Common Schema (ECS) specification, one can use the following configuration:

```xml
...
<Console name="console" target="SYSTEM_OUT">
    <JsonTemplateLayout eventTemplateUri="classpath:EcsLayout.json" />
</Console>
<CasAppender name="casConsole">
    <AppenderRef ref="console" />
</CasAppender>
...
```

The generated JSON should match the following:

```json
{
  "@timestamp": "2017-05-25T19:56:23.370Z",
  "ecs.version": "1.2.0",
  "log.level": "ERROR",
  "message": "Hello, error!",
  "process.thread.name": "main",
  "log.logger": "org.apache.logging.log4j.JsonTemplateLayoutDemo",
  "error.type": "java.lang.RuntimeException",
  "error.message": "test",
  "error.stack_trace": "java.lang.RuntimeException: ...\n"
}
```
