package org.apereo.cas.version;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.spring.boot.mongo.JaversMongoAutoConfiguration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link EntityHistoryEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("RegisteredService")
@ExtendWith(CasTestExtension.class)
@AutoConfigureMockMvc
@SpringBootTestAutoConfigurations
@ImportAutoConfiguration(exclude = JaversMongoAutoConfiguration.class)
@SpringBootTest(classes = {
    BaseEntityHistoryTests.SharedTestConfiguration.class,
    EntityHistoryEndpointTests.EntityHistoryTestConfiguration.class
},
    properties = {
        "management.endpoints.access.default=UNRESTRICTED",
        "management.endpoints.web.exposure.include=*",
        "management.endpoint.entityHistory.access=UNRESTRICTED"
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class EntityHistoryEndpointTests extends BaseEntityHistoryTests {
    @Autowired
    @Qualifier("mockMvc")
    private MockMvc mockMvc;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier(ServiceRegistry.BEAN_NAME)
    private ServiceRegistry serviceRegistry;

    @Autowired
    @Qualifier("objectVersionRepository")
    private EntityHistoryRepository objectVersionRepository;

    @Test
    void verifyForbiddenOperation() throws Throwable {
        mockMvc.perform(get("/actuator/entityHistory/registeredServices/112233")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden());
    }

    @Test
    void verifyHistory() throws Throwable {
        val service1 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val savedService = servicesManager.save(service1);
        mockMvc.perform(get("/actuator/entityHistory/registeredServices/" + savedService.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    void verifyChangelog() throws Throwable {
        val service1 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val savedService = servicesManager.save(service1);
        mockMvc.perform(get("/actuator/entityHistory/registeredServices/" + savedService.getId() + "/changelog"))
            .andExpect(status().isOk());
    }

    @Test
    void verifyRestore() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        service.setDescription("Original service");
        service.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(List.of("mail", "uid")));
        servicesManager.save(service);
        val original = objectVersionRepository.getHistory(service).getFirst();

        service.setDescription("Updated service");
        service.setAttributeReleasePolicy(new DenyAllAttributeReleasePolicy());
        servicesManager.save(service);
        val updated = objectVersionRepository.getHistory(service).getFirst();

        mockMvc.perform(post("/actuator/entityHistory/registeredServices/{id}/restore/{version}", service.getId(), original.id())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(service.getId()))
            .andExpect(jsonPath("$.description").value("Original service"));

        val restored = servicesManager.findServiceBy(service.getId());
        assertEquals(original.entity(), restored);
        assertEquals(original.entity(), serviceRegistry.findServiceById(service.getId()));
        val policy = assertInstanceOf(ReturnAllowedAttributeReleasePolicy.class, restored.getAttributeReleasePolicy());
        assertEquals(List.of("mail", "uid"), policy.getAllowedAttributes());

        val history = objectVersionRepository.getHistory(restored);
        assertEquals(3, history.size());
        assertNotEquals(original.id(), history.getFirst().id());
        assertEquals(original.entity(), history.getFirst().entity());
        assertEquals(updated, history.get(1));
        assertEquals(original, history.getLast());

        mockMvc.perform(post("/actuator/entityHistory/registeredServices/{id}/restore/{version}", service.getId(), updated.id())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Updated service"));
        assertEquals(updated.entity(), serviceRegistry.findServiceById(service.getId()));
    }

    @Test
    void verifyRestoreUnknownRevision() throws Throwable {
        val service = servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString()));
        val history = objectVersionRepository.getHistory(service);
        mockMvc.perform(post("/actuator/entityHistory/registeredServices/{id}/restore/{version}", service.getId(), "unknown"))
            .andExpect(status().isNotFound());
        assertEquals(service, servicesManager.findServiceBy(service.getId()));
        assertEquals(service, serviceRegistry.findServiceById(service.getId()));
        assertEquals(history, objectVersionRepository.getHistory(service));
    }

    @Test
    void verifyRestoreRevisionFromAnotherService() throws Throwable {
        val service = servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString()));
        val other = servicesManager.save(RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString()));
        val otherRevision = objectVersionRepository.getHistory(other).getFirst();
        val history = objectVersionRepository.getHistory(service);
        mockMvc.perform(post("/actuator/entityHistory/registeredServices/{id}/restore/{version}", service.getId(), otherRevision.id()))
            .andExpect(status().isNotFound());
        assertEquals(service, serviceRegistry.findServiceById(service.getId()));
        assertEquals(other, serviceRegistry.findServiceById(other.getId()));
        assertEquals(history, objectVersionRepository.getHistory(service));
    }

    @Test
    void verifyRestoreUnknownService() throws Throwable {
        mockMvc.perform(post("/actuator/entityHistory/registeredServices/{id}/restore/{version}", Long.MAX_VALUE, "1.00"))
            .andExpect(status().isForbidden());
    }

    @Test
    void verifyRestoreDisabledService() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(service);
        val original = objectVersionRepository.getHistory(service).getFirst();
        service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(false, false));
        servicesManager.save(service);
        val history = objectVersionRepository.getHistory(service);
        mockMvc.perform(post("/actuator/entityHistory/registeredServices/{id}/restore/{version}", service.getId(), original.id()))
            .andExpect(status().isForbidden());
        assertEquals(service, serviceRegistry.findServiceById(service.getId()));
        assertEquals(history, objectVersionRepository.getHistory(service));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class EntityHistoryTestConfiguration {
        @Bean
        public MongoDatabaseFactory javersMongoDatabaseFactory() {
            return mock(MongoDatabaseFactory.class);
        }

        @Bean(name = "JaversFromStarter")
        public Javers javers() {
            return JaversBuilder.javers().build();
        }
    }
}
