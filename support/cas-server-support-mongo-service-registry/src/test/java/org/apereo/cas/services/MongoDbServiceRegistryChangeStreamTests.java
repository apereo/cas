package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.config.CasMongoDbServiceRegistryAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoDbServiceRegistryChangeStreamTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@SpringBootTest(classes = {
    CasMongoDbServiceRegistryAutoConfiguration.class,
    AbstractServiceRegistryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.service-registry.mongo.client-uri=mongodb://root:secret@localhost:37017,localhost:37018,localhost:37019/cas?authSource=admin&replicaSet=rs0",
        "cas.service-registry.mongo.collection=" + MongoDbServiceRegistryChangeStreamTests.COLLECTION,
        "cas.service-registry.mongo.drop-collection=true",
        "cas.service-registry.mongo.change-stream.quiet-period=PT0.2S"
    })
@Tag("MongoDb")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = {37017, 37018, 37019})
class MongoDbServiceRegistryChangeStreamTests {
    static final String COLLECTION = "cas-service-registry-change-stream";

    @Autowired
    @Qualifier("mongoDbServiceRegistryChangeStreamWatcher")
    private MongoDbServiceRegistryChangeStreamWatcher watcher;

    @Autowired
    @Qualifier("mongoDbServiceRegistryTemplate")
    private MongoOperations mongoDbServiceRegistryTemplate;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    void verifyChangesMadeOutsideCasAreLoaded() {
        assertTrue(watcher.isRunning());
        servicesManager.save(buildService());

        val external = buildService();
        external.assignIdIfNecessary();
        /*
            Saved again on every poll: the stream opens in the background after startup,
            and a change made before it opens is only seen by the scheduled reload.
         */
        await().pollInterval(Duration.ofSeconds(1)).atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            mongoDbServiceRegistryTemplate.save(external, COLLECTION);
            assertTrue(isCached(external.getId()));
        });

        mongoDbServiceRegistryTemplate.remove(new Query(Criteria.where("id").is(external.getId())), RegisteredService.class, COLLECTION);
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> assertFalse(isCached(external.getId())));
    }

    private boolean isCached(final long id) {
        return servicesManager.getAllServices().stream().anyMatch(service -> service.getId() == id);
    }

    private static CasRegisteredService buildService() {
        val id = UUID.randomUUID().toString();
        val service = new CasRegisteredService();
        service.setServiceId("https://%s.example.org".formatted(id));
        service.setName(id);
        return service;
    }
}
