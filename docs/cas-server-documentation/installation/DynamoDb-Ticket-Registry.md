---
layout: default
title: CAS - DynamoDb Ticket Registry
---

# DynamoDb Ticket Registry

DynamoDb ticket registry integration is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-dynamodb-ticket-registry</artifactId>
     <version>${cas.version}</version>
</dependency>
```

This registry stores tickets in [DynamoDb](https://aws.amazon.com/dynamodb/) instances.

## Configuration

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#dynamodb-ticket-registry).

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<AsyncLogger name="com.amazonaws" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</AsyncLogger>
...
```
