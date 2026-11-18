# Ticket Registry Replication Encryption

Source: https://apereo.github.io/cas/development/installation/Ticket-Registry-Replication-Encryption.html

The following ticket registries are able to support secure ticket replication by encrypting and signing tickets:

- [Hazelcast](https://apereo.github.io/cas/development/ticketing/Hazelcast-Ticket-Registry.html)
- [Apache Ignite](https://apereo.github.io/cas/development/ticketing/Ignite-Ticket-Registry.html)
- [Apache Geode](https://apereo.github.io/cas/development/ticketing/Geode-Ticket-Registry.html)
- [Redis](https://apereo.github.io/cas/development/ticketing/Redis-Ticket-Registry.html)
- [MongoDb](https://apereo.github.io/cas/development/ticketing/MongoDb-Ticket-Registry.html)

> **Default Behavior**
>
> Encryption by default is turned off when you use the above ticket registries. It requires explicit configuration before it can be used.

## Configuration

Each ticket registry configuration supports a cipher component that needs to be configured by the deployer. The settings, algorithms and secret keys used for the cipher may be controlled via CAS settings. Refer to the settings allotted for each registry to learn more about ticket encryption.
