package org.jasig.cas.config;

import org.jasig.cas.couchbase.core.CouchbaseClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.StringUtils;

/**
 * This is {@link CouchbaseServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("couchbaseServiceRegistryConfiguration")
@Lazy(true)
public class CouchbaseServiceRegistryConfiguration {

    /**
     * The Node set.
     */
    @Value("${svcreg.couchbase.nodes:localhost:8091}")
    private String nodeSet;

    /**
     * The Timeout.
     */
    @Value("${svcreg.couchbase.timeout:10}")
    private int timeout;

    /**
     * The Password.
     */
    @Value("${svcreg.couchbase.password:}")
    private String password;

    /**
     * The Bucket.
     */
    @Value("${svcreg.couchbase.bucket:default}")
    private String bucket;

    /**
     * Service registry couchbase client factory couchbase client factory.
     *
     * @return the couchbase client factory
     */
    @RefreshScope
    @Bean(name = "serviceRegistryCouchbaseClientFactory")
    public CouchbaseClientFactory serviceRegistryCouchbaseClientFactory() {
        final CouchbaseClientFactory factory = new CouchbaseClientFactory();
        factory.setNodes(StringUtils.commaDelimitedListToSet(this.nodeSet));
        factory.setTimeout(this.timeout);
        factory.setBucket(this.bucket);
        factory.setPassword(this.password);
        return factory;
    }
}
