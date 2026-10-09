package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mongo.serviceregistry.MongoDbServiceRegistryProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MongoDbServiceRegistryChangeStreamWatcherTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("MongoDb")
class MongoDbServiceRegistryChangeStreamWatcherTests {

    @Test
    void verifyReplicaSetDefinition() {
        assertTrue(isReplicaSetDefined(properties -> properties.setReplicaSet("rs0")));
        assertFalse(isReplicaSetDefined(properties -> properties.setHost("localhost")));
        assertTrue(isReplicaSetDefined(properties -> properties.setClientUri("mongodb://host1,host2/cas?replicaSet=rs0")));
        assertFalse(isReplicaSetDefined(properties -> properties.setClientUri("mongodb://localhost/cas")));
        assertTrue(isReplicaSetDefined(properties -> properties.setClientUri("mongodb+srv://cluster.example.org/cas")));
        assertFalse(isReplicaSetDefined(properties -> properties.setClientUri("mongodb://localhost/cas").setReplicaSet("rs0")));
    }

    @Test
    void verifyWatcherDoesNotStartWhenDisabledOrUndefined() {
        val casProperties = new CasConfigurationProperties();
        val mongo = casProperties.getServiceRegistry().getMongo();
        val watcher = new MongoDbServiceRegistryChangeStreamWatcher(mock(ObjectProvider.class), mock(ObjectProvider.class), casProperties);

        watcher.start();
        assertFalse(watcher.isRunning());

        mongo.setReplicaSet("rs0");
        mongo.getChangeStream().setEnabled(false);
        watcher.start();
        assertFalse(watcher.isRunning());
    }

    private static boolean isReplicaSetDefined(final Consumer<MongoDbServiceRegistryProperties> configurer) {
        val properties = new MongoDbServiceRegistryProperties();
        configurer.accept(properties);
        return MongoDbServiceRegistryChangeStreamWatcher.isReplicaSetDefined(properties);
    }
}
