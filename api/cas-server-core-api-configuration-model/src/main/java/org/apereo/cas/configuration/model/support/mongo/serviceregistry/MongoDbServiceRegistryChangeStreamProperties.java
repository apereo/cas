package org.apereo.cas.configuration.model.support.mongo.serviceregistry;

import module java.base;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link MongoDbServiceRegistryChangeStreamProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-mongo-service-registry")
@Getter
@Setter
@Accessors(chain = true)
public class MongoDbServiceRegistryChangeStreamProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 5718826329426716340L;

    /**
     * Watch the service registry collection through a MongoDb change stream and reload
     * service definitions as soon as they change, on every CAS node. This only takes effect
     * when a replica set is defined, either with the {@code replica-set} setting or in the
     * {@code client-uri}, and the server confirms that it is a member of a replica set.
     * The scheduled reload of service definitions remains in place as a fallback.
     */
    private boolean enabled = true;

    /**
     * How long to wait after the last observed change before reloading service definitions,
     * so that a burst of changes, such as a bulk import, results in a single reload.
     */
    @DurationCapable
    private String quietPeriod = "PT2S";
}
