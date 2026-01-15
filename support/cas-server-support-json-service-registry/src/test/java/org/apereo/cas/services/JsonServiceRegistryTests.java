package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.AbstractResourceBasedServiceRegistry;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import tools.jackson.core.TokenStreamFactory;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.dataformat.cbor.CBORFactory;
import tools.jackson.dataformat.smile.SmileFactory;
import tools.jackson.dataformat.yaml.YAMLFactory;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Handles test cases for {@link JsonServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Tag("FileSystem")
@ExtendWith(CasTestExtension.class)
@Slf4j
@SpringBootTest(classes = AbstractServiceRegistryTests.SharedTestConfiguration.class)
class JsonServiceRegistryTests extends BaseResourceBasedServiceRegistryTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Override
    public ResourceBasedServiceRegistry getNewServiceRegistry() {
        this.newServiceRegistry = buildResourceBasedServiceRegistry(RESOURCE);
        return newServiceRegistry;
    }

    @Test
    void verifyNativeImageServicesDirectory() throws Throwable {
        System.setProperty(CasRuntimeHintsRegistrar.SYSTEM_PROPERTY_SPRING_AOT_PROCESSING, "true");
        val resource = mock(Resource.class);
        when(resource.getURI()).thenReturn(new URI("resource:/services"));
        val registry = buildResourceBasedServiceRegistry(resource);
        val location = registry.getServiceRegistryDirectory();
        assertEquals(AbstractResourceBasedServiceRegistry.FALLBACK_REGISTERED_SERVICES_LOCATION.getCanonicalPath(), location.toFile().getCanonicalPath());
    }

    @Test
    void verifyRequiredHandlersServiceDefinition() throws Throwable {
        val resource = new ClassPathResource("RequiredHandlers-10000004.json");
        val serializer = new RegisteredServiceJsonSerializer(applicationContext);
        val service = serializer.from(resource.getInputStream());
        assertNotNull(service);
    }

    @Test
    void verifyExistingDefinitionForCompatibility2() throws Throwable {
        val resource = new ClassPathResource("returnMappedAttributeReleasePolicyTest2.json");
        val serializer = new RegisteredServiceJsonSerializer(applicationContext);
        val service = serializer.from(resource.getInputStream());
        assertNotNull(service);
        assertNotNull(service.getAttributeReleasePolicy());
        val policy = (ReturnMappedAttributeReleasePolicy) service.getAttributeReleasePolicy();
        assertNotNull(policy);
        assertEquals(2, policy.getAllowedAttributes().size());
    }

    @Test
    void verifyExistingDefinitionForCompatibility1() throws Throwable {
        val resource = new ClassPathResource("returnMappedAttributeReleasePolicyTest1.json");
        val serializer = new RegisteredServiceJsonSerializer(applicationContext);
        val service = serializer.from(resource.getInputStream());
        assertNotNull(service);
        assertNotNull(service.getAttributeReleasePolicy());
        val policy = (ReturnMappedAttributeReleasePolicy) service.getAttributeReleasePolicy();
        assertNotNull(policy);
        assertEquals(2, policy.getAllowedAttributes().size());
    }

    @Test
    void verifyUsernameProviderWithAttributeReleasePolicy() throws Throwable {
        val resource = new ClassPathResource("UsernameAttrRelease-100.json");
        val serializer = new RegisteredServiceJsonSerializer(applicationContext);
        val service = serializer.from(resource.getInputStream());
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(service)
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
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
            .applicationContext(applicationContext)
            .build();
        val username = service.getUsernameAttributeProvider().resolveUsername(usernameContext);
        assertEquals("casuser", username);
    }

    @ParameterizedTest
    @MethodSource("getObjectMapperFactories")
    void verifySerializationPerformance(final TokenStreamFactory factory) {
        val mapperFactory = JacksonObjectMapperFactory.builder().jsonFactory(factory).build();
        val mapper = mapperFactory.toObjectMapper();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val stopWatch = new StopWatch();
        stopWatch.start();
        for (var i = 0; i < 100; i++) {
            val written = mapper.writeValueAsBytes(registeredService);
            assertNotNull(written);
            val result = mapper.readValue(written, RegisteredService.class);
            assertNotNull(result);
        }
        LOGGER.debug("[{}]->[{}]", factory.getClass().getSimpleName(), stopWatch.getDuration());
        stopWatch.stop();
    }
    
    @Override
    protected Stream<Class<? extends BaseWebBasedRegisteredService>> getRegisteredServiceTypes() {
        return Stream.of(
            CasRegisteredService.class,
            OAuthRegisteredService.class,
            SamlRegisteredService.class,
            OidcRegisteredService.class,
            WSFederationRegisteredService.class
        );
    }

    private AbstractResourceBasedServiceRegistry buildResourceBasedServiceRegistry(final Resource location) {
        return new JsonServiceRegistry(location, WatcherService.noOp(),
            applicationContext,
            new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy(),
            new ArrayList<>());
    }

    public static Stream<Arguments> getObjectMapperFactories() {
        return Stream.of(
            Arguments.of(new JsonFactory()),
            Arguments.of(new YAMLFactory()),
            Arguments.of(new CBORFactory()),
            Arguments.of(new SmileFactory())
        );
    }
}
