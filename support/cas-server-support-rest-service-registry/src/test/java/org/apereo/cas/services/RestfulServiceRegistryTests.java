package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRestServiceRegistryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is {@link RestfulServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RestfulServiceRegistryTests.RestfulServiceRegistryTestConfiguration.class,
    AbstractServiceRegistryTests.SharedTestConfiguration.class,
    CasRestServiceRegistryAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreWebflowAutoConfiguration.class
},
    properties = {
        "server.port=9303",
        "cas.service-registry.rest.url=http://localhost:9303",
        "cas.service-registry.core.init-from-json=false"
    },
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("RestfulApi")
@ExtendWith(CasTestExtension.class)
@Getter
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RestfulServiceRegistryTests extends AbstractServiceRegistryTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier("restfulServiceRegistry")
    private ServiceRegistry newServiceRegistry;

    @TestConfiguration(value = "RestfulServiceRegistryTestConfiguration", proxyBeanMethods = false)
    static class RestfulServiceRegistryTestConfiguration {

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @RestController("servicesController")
        @RequestMapping("/")
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
                        public ResponseEntity save(@RequestBody final String service) throws Exception {
                val registeredService = MAPPER.readValue(service, RegisteredService.class);
                serviceRegistry.save(registeredService);
                return ResponseEntity.ok(MAPPER.writeValueAsString(registeredService));
            }

            @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
                        public ResponseEntity findServiceById(@PathVariable(name = "id") final String id) throws Exception {
                if (NumberUtils.isParsable(id)) {
                    return ResponseEntity.ok(MAPPER.writeValueAsString(serviceRegistry.findServiceById(Long.parseLong(id))));
                }
                return ResponseEntity.ok(MAPPER.writeValueAsString(serviceRegistry.findServiceByExactServiceId(id)));
            }

            @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
                        public ResponseEntity load() throws Exception {
                return ResponseEntity.ok(MAPPER.writeValueAsString(serviceRegistry.load()));
            }
        }
    }

}
