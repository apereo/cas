---
layout: default
title: CAS - Cassandra Ticket Registry
---

# Cassandra Ticket Registry
Cassandra integration is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-cassandra-ticket-registry</artifactId>
    <version>${cas.version}</version>
</dependency>
```

## Configuration Considerations

There are four core configuration concerns with Cassandra:

1. Keyspace
2. Object serialization mechanism
3. Multi datacenter replication

### Keyspace
We suggest the following Keysapace definition:

- String serialized ticket:
```cql
CREATE KEYSPACE cas WITH replication = {'class': 'NetworkTopologyStrategy', 'replication_factor': '3'}  AND durable_writes = true;

CREATE TABLE IF NOT EXISTS cas.ticketgrantingticket (
    id text PRIMARY KEY,
    ticket text,
    ticket_granting_ticket_id text,
    expiration_bucket bigint
) WITH default_time_to_live = 5184000;

CREATE MATERIALIZED VIEW IF NOT EXISTS cas.ticket_cleaner AS
SELECT expiration_bucket, ticket, id FROM ticketgrantingticket
WHERE id IS NOT NULL AND expiration_bucket IS NOT NULL AND ticket IS NOT NULL
PRIMARY KEY (expiration_bucket, id);

CREATE TABLE IF NOT EXISTS cas.serviceticket (
    id text PRIMARY KEY,
    ticket text
) WITH default_time_to_live = 60;

CREATE TABLE IF NOT EXISTS cas.ticket_cleaner_lastrun (
    id text PRIMARY KEY,
    last_run bigint
);
```

### Object Serialization
Our Cassandra ticket registry implementation stores tickets as String, so CAS tickets must be serialized to a byte array prior to storage. 
CAS ships with a custom serialization component, `JacksonJsonSerializer`. 


### Multi datacenter replication
Cassandra specify the replication factor in the [Keyspace definition](http://docs.datastax.com/en/cql/3.1/cql/cql_reference/create_keyspace_r.html).
In order to allow multi datacenter replication just for ticketGrantingTickets, we allow to separate our data in two keyspaces, the one that will be replicated through all the datacenters and the other that will not.
This keyspaces name can be specified using fully qualified table names (keyspace.table_name) in the Configuration properties.

Given the ServiceTicket short life nature, we suggest not to replicate this table.

## Configuration

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).

## High Availability Considerations
Read more about Cassandra performance and high availability [here](http://cassandra.apache.org/)