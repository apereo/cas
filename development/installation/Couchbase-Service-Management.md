---
layout: default
title: CAS - Couchbase Service Registry
---

# Couchbase Service Registry
Couchbase integration is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-couchbase-service-registry</artifactId>
     <version>${cas.version}</version>
</dependency>
```

Enable the registry in `application.properties` via:

```properties
#CAS components mappings
serviceRegistryDao=couchbaseServiceRegistryDao
```

[Couchbase](http://www.couchbase.com) is a highly available, open source NoSQL database server based on 
[Erlang/OTP](http://www.erlang.org) and its mnesia database. The intention of this registry is to leverage the capability of Couchbase 
server to provide high availability to CAS.

## Configuration
The following settings are available:

```properties
svcreg.couchbase.nodes=
svcreg.couchbase.bucket=
svcreg.couchbase.password=
```

The Couchbase integration currently assumes that the service registries are stored
in their own buckets. Optionally set passwords for the buckets, optionally setup
redundancy and replication as per normal Couchbase configuration.

The only truly mandatory setting is the list of nodes.
The other settings are optional, but this is designed to store data in buckets
so in reality the bucket property must also be set.

## Auto Initialization

Upon startup and if the services registry database is blank, 
the registry is able to auto initialize itself from default 
JSON service definitions available to CAS. This behavior can be controlled via:

```properties
# svcreg.database.from.json=false
```
