package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRestServiceRegistryAutoConfiguration;
import org.apereo.cas.config.CasThemesAutoConfiguration;
import org.apereo.cas.config.CasWebAppAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.services.RestfulServiceRegistryProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    AbstractServiceRegistryTests.SharedTestConfiguration.class,
    RestfulServiceRegistryTests.RestfulServiceRegistryTestConfiguration.class,
    CasRestServiceRegistryAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasThemesAutoConfiguration.class,
    CasWebAppAutoConfiguration.class
},
    properties = "cas.service-registry.core.init-from-json=false",
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = RestfulServiceRegistryTests.PortInitializer.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("RestfulApi")
@ExtendWith(CasTestExtension.class)
@Getter
class RestfulServiceRegistryTests extends AbstractServiceRegistryTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    static class PortInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(final ConfigurableApplicationContext context) {
            val port = findAvailableTcpPort();
            TestPropertyValues.of(
                "server.port=" + port,
                "cas.service-registry.rest.url=http://localhost:%s/casservices".formatted(port)
            ).applyTo(context.getEnvironment());
        }

        private static int findAvailableTcpPort() {
            try (val socket = new ServerSocket(0)) {
                socket.setReuseAddress(true);
                return socket.getLocalPort();
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Autowired
    @Qualifier("restfulServiceRegistry")
    private ServiceRegistry newServiceRegistry;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyLoadReturnsListenerResults() {
        val registeredService = new CasRegisteredService();
        registeredService.setId(RandomUtils.nextLong());
        registeredService.setName(UUID.randomUUID().toString());
        registeredService.setServiceId("https://" + registeredService.getName() + ".example.org");
        newServiceRegistry.save(registeredService);

        val listener = new ServiceRegistryListener() {
            @Serial
            private static final long serialVersionUID = 4515471049423402212L;

            @Override
            public RegisteredService postLoad(final RegisteredService loaded) {
                val replacement = new CasRegisteredService();
                replacement.setId(loaded.getId());
                replacement.setName(loaded.getName());
                replacement.setServiceId(loaded.getServiceId());
                replacement.setDescription("reconciled");
                return replacement;
            }
        };
        val properties = applicationContext.getBean(CasConfigurationProperties.class).getServiceRegistry().getRest();
        val registry = new RestfulServiceRegistry(applicationContext, List.of(listener), properties);
        val loaded = registry.load().stream()
            .filter(service -> service.getId() == registeredService.getId())
            .findFirst()
            .orElseThrow();
        assertEquals("reconciled", loaded.getDescription());
    }

    @Test
    void verifyReportedUrlHasNoCredentials() {
        assertEquals("https://registry.example.org/services",
            RestfulServiceRegistry.sanitizeUrl("https://user:secret@registry.example.org/services?token=abc#frag"));
        assertEquals("http://localhost:8080/casservices",
            RestfulServiceRegistry.sanitizeUrl("http://localhost:8080/casservices"));
        assertEquals("[unparsable url]", RestfulServiceRegistry.sanitizeUrl("http://bad url"));
    }

    @Test
    void verifyFailedLoadIsReported() {
        try (val webServer = new MockWebServer(HttpStatus.INTERNAL_SERVER_ERROR)) {
            webServer.start();
            val properties = new RestfulServiceRegistryProperties();
            properties.setUrl("http://localhost:%s".formatted(webServer.getPort()));
            properties.setMaximumRetryAttempts(0);
            val registry = new RestfulServiceRegistry(applicationContext, List.of(), properties);
            assertThrows(IllegalStateException.class, registry::load);
        }
    }


    @TestConfiguration(value = "RestfulServiceRegistryTestConfiguration", proxyBeanMethods = false)
    static class RestfulServiceRegistryTestConfiguration {

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Bean
        public CasWebSecurityConfigurer<Void> casServicesEndpointConfigurer() {
            return new CasWebSecurityConfigurer<>() {
                @Override
                public List<String> getIgnoredEndpoints() {
                    return List.of("/casservices");
                }
            };
        }

        @RestController("servicesController")
        @RequestMapping("/casservices")
        public class ServicesController {
            private final InMemoryServiceRegistry serviceRegistry = new InMemoryServiceRegistry(applicationContext);

            @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
            public ResponseEntity delete(@PathVariable(name = "id") final Long id) {
                val service = serviceRegistry.findServiceById(id);
                serviceRegistry.delete(service);
                return ResponseEntity.ok().build();
            }

            @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
            public ResponseEntity delete() {
                serviceRegistry.deleteAll();
                return ResponseEntity.ok().build();
            }

            @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
            public ResponseEntity save(@RequestBody final String service) {
                val registeredService = MAPPER.readValue(service, RegisteredService.class);
                serviceRegistry.save(registeredService);
                return ResponseEntity.ok(MAPPER.writeValueAsString(registeredService));
            }

            @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
            public ResponseEntity findServiceById(@PathVariable(name = "id") final String id) {
                if (NumberUtils.isParsable(id)) {
                    return ResponseEntity.ok(MAPPER.writeValueAsString(serviceRegistry.findServiceById(Long.parseLong(id))));
                }
                return ResponseEntity.ok(MAPPER.writeValueAsString(serviceRegistry.findServiceByExactServiceId(id)));
            }

            @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
            public ResponseEntity load() {
                return ResponseEntity.ok(MAPPER.writeValueAsString(serviceRegistry.load()));
            }
        }
    }

}
