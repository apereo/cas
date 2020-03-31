package org.apereo.cas.configuration.model.support.couchbase.serviceregistry;

import org.apereo.cas.configuration.model.support.couchbase.BaseCouchbaseProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link CouchbaseServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-couchbase-service-registry")
@Getter
@Setter
@Accessors(chain = true)
public class CouchbaseServiceRegistryProperties extends BaseCouchbaseProperties {
    private static final long serialVersionUID = -4975171412161962007L;
}
