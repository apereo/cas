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

There are three core configuration concerns with memcached:

1. Keyspace
2. Ticket cleaning
3. Object serialization mechanism

### Keyspace
We suggest the following Keysapace definition:

- String serialized ticket:
```cql
CREATE KEYSPACE cas WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

CREATE TABLE cas.ticket_cleaner (
    expiry_type text,
    date_bucket bigint,
    id text,
    PRIMARY KEY ((expiry_type, date_bucket), id)
);

CREATE TABLE cas.ticketgrantingticket (
    id text PRIMARY KEY,
    ticket text
) WITH default_time_to_live = 5184000;

CREATE TABLE cas.serviceticket (
    id text PRIMARY KEY,
    ticket text
) WITH default_time_to_live = 60;

CREATE TABLE cas.ticket_cleaner_lastrun (
    id text PRIMARY KEY,
    last_run bigint
);
```

- Binary serialized ticket:
```cql
CREATE KEYSPACE cas WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

CREATE TABLE cas.ticket_cleaner (
    expiry_type text,
    date_bucket bigint,
    id text,
    PRIMARY KEY ((expiry_type, date_bucket), id)
);

CREATE TABLE cas.ticketgrantingticket (
    id text PRIMARY KEY,
    ticket blob
) WITH default_time_to_live = 5184000;

CREATE TABLE cas.serviceticket (
    id text PRIMARY KEY,
    ticket blob
) WITH default_time_to_live = 60;

CREATE TABLE cas.ticket_cleaner_lastrun (
    id text PRIMARY KEY,
    last_run bigint
);
```

### Ticket cleaning
Cassandra supports [TTL](https://en.wikipedia.org/wiki/Time_to_live). We use this feature to clean up Service Tickets as we know the maximum duration.
This TTL has to be defined in the schema definition:
```cql
CREATE TABLE cas.serviceticket (
    id text PRIMARY KEY,
    ticket text
) WITH default_time_to_live = 60;
```

For TicketGrantingTickets we follow a different approach. When a new TGT is stored, its id is stored in the ticket_cleaner table, within a bucket based on the ticket expiration time.
This time bucket is 10 seconds by default.
The cleaner will check the last bucket period, and retrieve all the ticket's id for the next bucket in the ticket_cleaner table, then it'll run queries agains the TGT table in order to retrieve the whole ticket, and run the cleaning process itself:
- check if expired, and if so
- logout user from services
- remove ticket

### Object Serialization
Our Cassandra ticket registry implementation can store tickets as String or bytes of data, so CAS tickets must be serialized to a byte array prior to storage. 
CAS ships with two custom serialization components `JacksonBinarySerializer` and `JacksonJsonSerializer`. By default `JacksonJsonSerializer` is used, but you 
can use the other passing it to `CassandraDao`. 


## Configuration

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html).

## High Availability Considerations
