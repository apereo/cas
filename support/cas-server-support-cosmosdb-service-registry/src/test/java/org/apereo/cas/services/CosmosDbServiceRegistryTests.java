package org.apereo.cas.services;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CosmosDbServiceRegistryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import javax.net.ssl.HttpsURLConnection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CosmosDbServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("CosmosDb")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    RefreshAutoConfiguration.class,
    CosmosDbServiceRegistryConfiguration.class
}, properties = {
    "cas.http-client.host-name-verifier=none",
    "cas.service-registry.cosmosDb.uri=https://localhost:8081",
    "cas.service-registry.cosmosDb.key=C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==",
    "cas.service-registry.cosmosDb.database=RegisteredServicesDb",
    "cas.service-registry.cosmosDb.max-retry-attempts-on-throttled-requests=1",
    "cas.service-registry.cosmosDb.indexing-mode=CONSISTENT",
    "cas.service-registry.cosmosDb.drop-container=true"
})
@ResourceLock("cosmosdb-service")
@Getter
@EnabledIfPortOpen(port = 8081)
public class CosmosDbServiceRegistryTests extends AbstractServiceRegistryTests {
    static {
        HttpsURLConnection.setDefaultHostnameVerifier(CasSSLContext.disabled().getHostnameVerifier());
        HttpsURLConnection.setDefaultSSLSocketFactory(CasSSLContext.disabled().getSslContext().getSocketFactory());
    }

    @Autowired
    @Qualifier("cosmosDbServiceRegistry")
    private ServiceRegistry newServiceRegistry;

    @BeforeEach
    public void deleteAll() {
        newServiceRegistry.load().forEach(service -> newServiceRegistry.delete(service));
        assertTrue(newServiceRegistry.load().isEmpty());
    }
}
