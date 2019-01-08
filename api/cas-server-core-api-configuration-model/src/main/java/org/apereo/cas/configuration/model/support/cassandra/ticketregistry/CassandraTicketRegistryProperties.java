package org.apereo.cas.configuration.model.support.cassandra.ticketregistry;

import org.apereo.cas.configuration.model.support.cassandra.authentication.BaseCassandraProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;

@RequiresModule(name = "cas-server-support-cassandra-ticket-registry")
@Getter
@Setter
public class CassandraTicketRegistryProperties extends BaseCassandraProperties {
}