package org.apereo.cas.version;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link EntityHistoryRepositoryMongoDbTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("MongoDb")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 27017)
@SpringBootTest(classes = BaseEntityHistoryTests.SharedTestConfiguration.class,
    properties = "cas.javers.mongo.client-uri=mongodb://root:secret@localhost:27017/cas?authSource=admin")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class EntityHistoryRepositoryMongoDbTests {
    @Autowired
    @Qualifier("objectVersionRepository")
    private EntityHistoryRepository objectVersionRepository;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    void verifyOperation() {
        val service1 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(service1);

        service1.setDescription("This is changed");
        service1.setAttributeReleasePolicy(new DenyAllAttributeReleasePolicy());
        servicesManager.save(service1);

        val history = objectVersionRepository.getHistory(service1);
        assertEquals(2, history.size());

        history.forEach(item -> {
            assertNotNull(item.date());
            assertNotNull(item.entity());
            assertNotNull(item.id());
        });
        assertNotNull(objectVersionRepository.getChangelog(service1));
    }
}
