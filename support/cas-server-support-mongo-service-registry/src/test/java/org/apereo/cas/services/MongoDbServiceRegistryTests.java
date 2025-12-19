package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.config.CasMongoDbServiceRegistryAutoConfiguration;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoDbServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SpringBootTest(classes = {
    CasMongoDbServiceRegistryAutoConfiguration.class,
    AbstractServiceRegistryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.service-registry.mongo.database-name=service-registry",
        "cas.service-registry.mongo.host=localhost",
        "cas.service-registry.mongo.port=27017",
        "cas.service-registry.mongo.user-id=root",
        "cas.service-registry.mongo.password=secret",
        "cas.service-registry.mongo.authentication-database-name=admin",
        "cas.service-registry.mongo.drop-collection=true"
    })
@Tag("MongoDb")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 27017)
@Getter
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MongoDbServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("mongoDbServiceRegistry")
    private ServiceRegistry newServiceRegistry;

    @Test
    void verifySamlServiceAttributeNames() {
        val service = new SamlRegisteredService();
        service.setName("TestAttributeNames");
        service.setDescription("Test Description");
        service.setServiceId("test.example.org");
        service.setMetadataLocation("https://test.example.org");
        service.setAttributeFriendlyNames(CollectionUtils.wrap("urn:oid:1.3.6.1.4.1.5923.1.1.1.10", "eduPersonTargetedID"));
        service.setAttributeNameFormats(CollectionUtils.wrap("urn:oid:1.3.6.1.4.1.5923.1.1.1.10", "uri"));
        getNewServiceRegistry().save(service);

        var newService = (SamlRegisteredService) getNewServiceRegistry().findServiceByExactServiceName(service.getName());
        assertNotNull(newService);
        assertFalse(newService.getAttributeFriendlyNames().isEmpty());
        assertFalse(newService.getAttributeNameFormats().isEmpty());
    }
}
