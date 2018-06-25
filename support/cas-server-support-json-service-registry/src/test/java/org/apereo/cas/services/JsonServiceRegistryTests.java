package org.apereo.cas.services;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;
import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Handles test cases for {@link JsonServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@RunWith(Parameterized.class)
public class JsonServiceRegistryTests extends AbstractResourceBasedServiceRegistryTests {
    public JsonServiceRegistryTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }

    @Override
    @SneakyThrows
    public void initializeServiceRegistry() {
        this.dao = new JsonServiceRegistry(RESOURCE, true,
            mock(ApplicationEventPublisher.class),
            new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy());
        super.initializeServiceRegistry();
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() {
        return Arrays.asList(RegexRegisteredService.class);
    }

    @Test
    public void verifyLegacyServiceDefinition() throws Exception {
        final var resource = new ClassPathResource("Legacy-10000003.json");
        final var serializer = new DefaultRegisteredServiceJsonSerializer();
        final var service = serializer.from(resource.getInputStream());
        assertNotNull(service);
    }

    @Test
    public void verifyExistingDefinitionForCompatibility2() throws IOException {
        final Resource resource = new ClassPathResource("returnMappedAttributeReleasePolicyTest2.json");
        final var serializer = new DefaultRegisteredServiceJsonSerializer();
        final var service = serializer.from(resource.getInputStream());
        assertNotNull(service);
        assertNotNull(service.getAttributeReleasePolicy());
        final var policy = (ReturnMappedAttributeReleasePolicy) service.getAttributeReleasePolicy();
        assertNotNull(policy);
        assertEquals(2, policy.getAllowedAttributes().size());
    }

    @Test
    public void verifyExistingDefinitionForCompatibility1() throws IOException {
        final Resource resource = new ClassPathResource("returnMappedAttributeReleasePolicyTest1.json");
        final var serializer = new DefaultRegisteredServiceJsonSerializer();
        final var service = serializer.from(resource.getInputStream());
        assertNotNull(service);
        assertNotNull(service.getAttributeReleasePolicy());
        final var policy = (ReturnMappedAttributeReleasePolicy) service.getAttributeReleasePolicy();
        assertNotNull(policy);
        assertEquals(2, policy.getAllowedAttributes().size());
    }
}
