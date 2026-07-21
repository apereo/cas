package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.config.CasMongoDbTicketRegistryAutoConfiguration;
import org.apereo.cas.ha.ClusterTopologyManager;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoDbClusterTopologyManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("MongoDb")
@SpringBootTest(
    classes = {
        CasMongoDbTicketRegistryAutoConfiguration.class,
        BaseTicketRegistryTests.SharedTestConfiguration.class
    },
    properties = "cas.ticket.registry.mongo.client-uri=mongodb://root:secret@localhost:37017,localhost:37018,localhost:37019/cas?authSource=admin&replicaSet=rs0")
@EnabledIfListeningOnPort(port = {37017, 37018, 37019})
class MongoDbClusterTopologyManagerTests {
    @Autowired
    @Qualifier("mongoDbTicketClusterTopologyManager")
    private ClusterTopologyManager mongoDbTicketClusterTopologyManager;

    @Test
    void verifyOperation() throws Exception {
        val results = mongoDbTicketClusterTopologyManager.discoverMembers();
        assertFalse(results.isEmpty());
    }
}
