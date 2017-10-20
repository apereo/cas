package org.apereo.cas.configuration.model.support.couchbase.serviceregistry;

import org.apereo.cas.configuration.model.support.couchbase.BaseCouchbaseProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * This is {@link CouchbaseServiceRegistryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-couchbase-service-registry")
public class CouchbaseServiceRegistryProperties extends BaseCouchbaseProperties implements Serializable {
    private static final long serialVersionUID = -4975171412161962007L;
}
