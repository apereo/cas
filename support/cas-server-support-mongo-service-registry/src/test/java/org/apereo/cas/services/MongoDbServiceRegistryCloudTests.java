package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.MongoDbServiceRegistryConfiguration;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoDbServiceRegistryCloudTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SpringBootTest(classes = {
    MongoDbServiceRegistryConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreUtilConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.service-registry.mongo.databaseName=service-registry",
        "cas.service-registry.mongo.host=localhost",
        "cas.service-registry.mongo.port=27017",
        "cas.service-registry.mongo.userId=root",
        "cas.service-registry.mongo.password=secret",
        "cas.service-registry.mongo.authenticationDatabaseName=admin",
        "cas.service-registry.mongo.dropCollection=true"
    })
@Tag("MongoDb")
@EnabledIfPortOpen(port = 27017)
public class MongoDbServiceRegistryCloudTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("mongoDbServiceRegistry")
    private ServiceRegistry serviceRegistry;

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.serviceRegistry;
    }

    @Test
    public void verifySamlServiceAttributeNames() {
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
