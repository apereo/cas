package org.apereo.cas.couchbase.core;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CouchbaseClientFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Couchbase")
@EnabledIfPortOpen(port = 8091)
@SpringBootTest(classes = RefreshAutoConfiguration.class,
    properties = {
        "cas.authn.couchbase.clusterUsername=admin",
        "cas.authn.couchbase.clusterPassword=password",
        "cas.authn.couchbase.bucket=testbucket"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchbaseClientFactoryTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyInit() {
        val factory = new CouchbaseClientFactory(casProperties.getAuthn().getCouchbase());
        assertNotNull(factory.getConnectionTimeout());
        assertNotNull(factory.getKvTimeout());
        assertNotNull(factory.getProperties());
        assertNotNull(factory.getQueryTimeout());
        assertNotNull(factory.getSearchTimeout());
        assertNotNull(factory.getViewTimeout());
        assertNotNull(factory.getCluster());
    }
}
