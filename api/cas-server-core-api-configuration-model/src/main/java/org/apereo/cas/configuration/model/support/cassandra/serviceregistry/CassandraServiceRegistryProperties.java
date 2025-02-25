package org.apereo.cas.configuration.model.support.cassandra.serviceregistry;

import org.apereo.cas.configuration.model.support.cassandra.authentication.BaseCassandraProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link CassandraServiceRegistryProperties}.
 *
 * @since 6.1.0
 */
@RequiresModule(name = "cas-server-support-cassandra-service-registry")
@Getter
@Setter
@Accessors(chain = true)
public class CassandraServiceRegistryProperties extends BaseCassandraProperties {
    @Serial
    private static final long serialVersionUID = -1835394847251801709L;
}

