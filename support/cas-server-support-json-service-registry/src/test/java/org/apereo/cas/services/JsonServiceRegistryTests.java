package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.AbstractResourceBasedServiceRegistry;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
class JsonServiceRegistryTests extends BaseResourceBasedServiceRegistryTests {
    @Override
    public ResourceBasedServiceRegistry getNewServiceRegistry() throws Exception {
        this.newServiceRegistry = buildResourceBasedServiceRegistry(RESOURCE);
        return newServiceRegistry;
    }

    @Test
    void verifyNativeImageServicesDirectory() throws Exception {
        System.setProperty(CasRuntimeHintsRegistrar.SYSTEM_PROPERTY_SPRING_AOT_PROCESSING, "true");
        val resource = mock(Resource.class);
        when(resource.getURI()).thenReturn(new URI("resource:/services"));
        val registry = buildResourceBasedServiceRegistry(resource);
        val location = registry.getServiceRegistryDirectory();
        assertEquals(AbstractResourceBasedServiceRegistry.FALLBACK_REGISTERED_SERVICES_LOCATION.getCanonicalPath(), location.toFile().getCanonicalPath());
    }

    @Test
    void verifyRequiredHandlersServiceDefinition() throws Exception {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val resource = new ClassPathResource("RequiredHandlers-10000004.json");
        val serializer = new RegisteredServiceJsonSerializer(appCtx);
        val service = serializer.from(resource.getInputStream());
        assertNotNull(service);
    }

    @Test
    void verifyExistingDefinitionForCompatibility2() throws Exception {
        val resource = new ClassPathResource("returnMappedAttributeReleasePolicyTest2.json");
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val serializer = new RegisteredServiceJsonSerializer(appCtx);
        val service = serializer.from(resource.getInputStream());
        assertNotNull(service);
        assertNotNull(service.getAttributeReleasePolicy());
        val policy = (ReturnMappedAttributeReleasePolicy) service.getAttributeReleasePolicy();
        assertNotNull(policy);
        assertEquals(2, policy.getAllowedAttributes().size());
    }

    @Test
    void verifyExistingDefinitionForCompatibility1() throws Exception {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val resource = new ClassPathResource("returnMappedAttributeReleasePolicyTest1.json");
        val serializer = new RegisteredServiceJsonSerializer(appCtx);
        val service = serializer.from(resource.getInputStream());
        assertNotNull(service);
        assertNotNull(service.getAttributeReleasePolicy());
        val policy = (ReturnMappedAttributeReleasePolicy) service.getAttributeReleasePolicy();
        assertNotNull(policy);
        assertEquals(2, policy.getAllowedAttributes().size());
    }

    @Test
    void verifyUsernameProviderWithAttributeReleasePolicy() throws Exception {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        val resource = new ClassPathResource("UsernameAttrRelease-100.json");
        val serializer = new RegisteredServiceJsonSerializer(appCtx);
        val service = serializer.from(resource.getInputStream());
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(service)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(RegisteredServiceTestUtils.getPrincipal("casuser",
                Map.of("groups", List.of("g1", "g2"), "username", List.of("casuser"))))
            .build();
        val attributes = service.getAttributeReleasePolicy().getAttributes(context);
        assertEquals(3, attributes.size());
        assertTrue(attributes.containsKey("groups"));
        assertTrue(attributes.containsKey("username"));
        assertTrue(attributes.containsKey("familyName"));

        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .registeredService(context.getRegisteredService())
            .service(context.getService())
            .principal(context.getPrincipal())
            .build();
        val username = service.getUsernameAttributeProvider().resolveUsername(usernameContext);
        assertEquals("casuser", username);
    }

    private static AbstractResourceBasedServiceRegistry buildResourceBasedServiceRegistry(final Resource location) throws Exception {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        return new JsonServiceRegistry(location, WatcherService.noOp(),
            appCtx,
            new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy(),
            new ArrayList<>());
    }
}
