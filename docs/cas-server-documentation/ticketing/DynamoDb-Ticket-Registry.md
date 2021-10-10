---
layout: default
title: CAS - DynamoDb Ticket Registry
category: Ticketing
---

{% include variables.html %}

# DynamoDb Ticket Registry

DynamoDb ticket registry integration is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-dynamodb-ticket-registry" %}

This registry stores tickets in [DynamoDb](https://aws.amazon.com/dynamodb/) instances. 
Each ticket type is linked to a distinct table.

## Configuration

You will need to provide CAS with your [AWS credentials](https://aws.amazon.com/console/). Also, to gain a better understanding
of DynamoDb's core components and concepts, please [start with this guide](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Introduction.html) first.

{% include_cached casproperties.html properties="cas.ticket.registry.dynamo-db" %}

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="com.amazonaws" level="debug" additivity="false">
    <AppenderRef ref="console"/>
    <AppenderRef ref="file"/>
</Logger>
...
```
