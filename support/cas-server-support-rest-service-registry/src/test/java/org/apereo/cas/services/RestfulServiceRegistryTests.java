package org.apereo.cas.services;

import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.apereo.cas.config.RestServiceRegistryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
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
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    ServletWebServerFactoryAutoConfiguration.class,
    DispatcherServletAutoConfiguration.class,

    RestfulServiceRegistryTests.RestfulServiceRegistryTestConfiguration.class,

    CasCoreTicketsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    RestServiceRegistryConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCookieAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasWebflowAutoConfiguration.class
},
    properties = {
        "server.port=9303",
        "cas.service-registry.rest.url=http://localhost:9303",
        "cas.service-registry.core.init-from-json=false"
    },
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("RestfulApi")
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
