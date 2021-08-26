package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.io.WatcherService;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Handles test cases for {@link JsonServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Tag("FileSystem")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class
})
public class JsonServiceRegistryTests extends BaseResourceBasedServiceRegistryTests {
    @SneakyThrows
    @Override
    public ResourceBasedServiceRegistry getNewServiceRegistry() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        newServiceRegistry = new JsonServiceRegistry(RESOURCE, WatcherService.noOp(),
            appCtx,
            new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy(),
            new ArrayList<>());
        return newServiceRegistry;
    }

    @Test
    public void verifyRegistry() throws Exception {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val registry = new JsonServiceRegistry(RESOURCE, WatcherService.noOp(),
            appCtx,
            new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy(),
            new ArrayList<>());
        assertNotNull(registry.getName());
        assertNotNull(registry.getExtensions());
    }

    @Test
    public void verifyRequiredHandlersServiceDefinition() throws Exception {
        val resource = new ClassPathResource("RequiredHandlers-10000004.json");
        val serializer = new RegisteredServiceJsonSerializer();
        val service = serializer.from(resource.getInputStream());
        assertNotNull(service);
    }

    @Test
    public void verifyExistingDefinitionForCompatibility2() throws Exception {
        val resource = new ClassPathResource("returnMappedAttributeReleasePolicyTest2.json");
        val serializer = new RegisteredServiceJsonSerializer();
        val service = serializer.from(resource.getInputStream());
        assertNotNull(service);
        assertNotNull(service.getAttributeReleasePolicy());
        val policy = (ReturnMappedAttributeReleasePolicy) service.getAttributeReleasePolicy();
        assertNotNull(policy);
        assertEquals(2, policy.getAllowedAttributes().size());
    }

    @Test
    public void verifyExistingDefinitionForCompatibility1() throws Exception {
        val resource = new ClassPathResource("returnMappedAttributeReleasePolicyTest1.json");
        val serializer = new RegisteredServiceJsonSerializer();
        val service = serializer.from(resource.getInputStream());
        assertNotNull(service);
        assertNotNull(service.getAttributeReleasePolicy());
        val policy = (ReturnMappedAttributeReleasePolicy) service.getAttributeReleasePolicy();
        assertNotNull(policy);
        assertEquals(2, policy.getAllowedAttributes().size());
    }
}
