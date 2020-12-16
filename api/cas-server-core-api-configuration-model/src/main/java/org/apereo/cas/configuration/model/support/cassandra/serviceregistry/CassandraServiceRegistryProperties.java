package org.apereo.cas.configuration.model.support.cassandra.serviceregistry;

import org.apereo.cas.configuration.model.support.cassandra.authentication.BaseCassandraProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link CassandraServiceRegistryProperties}.
 *
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-cassandra-service-registry")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("CassandraServiceRegistryProperties")
public class CassandraServiceRegistryProperties extends BaseCassandraProperties {
    private static final long serialVersionUID = -1835394847251801709L;
}

