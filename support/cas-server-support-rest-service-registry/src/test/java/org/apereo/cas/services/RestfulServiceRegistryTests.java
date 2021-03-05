package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.RestServiceRegistryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;
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
    CasCoreServicesConfiguration.class,
    RestServiceRegistryConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreHttpConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class
},
    properties = {
        "server.port=9303",
        "cas.service-registry.rest.url=http://localhost:9303",
        "cas.service-registry.core.init-from-json=false"
    },
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableAutoConfiguration
@Tag("RestfulApi")
@Getter
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfulServiceRegistryTests extends AbstractServiceRegistryTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier("restfulServiceRegistry")
    private ServiceRegistry newServiceRegistry;

    @TestConfiguration
    @Lazy(false)
    public static class RestfulServiceRegistryTestConfiguration {

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
            @SneakyThrows
            public ResponseEntity save(@RequestBody final String service) {
                val registeredService = MAPPER.readValue(service, RegisteredService.class);
                serviceRegistry.save(registeredService);
                return ResponseEntity.ok(MAPPER.writeValueAsString(registeredService));
            }

            @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
            @SneakyThrows
            public ResponseEntity findServiceById(@PathVariable(name = "id") final String id) {
                if (NumberUtils.isParsable(id)) {
                    return ResponseEntity.ok(MAPPER.writeValueAsString(serviceRegistry.findServiceById(Long.parseLong(id))));
                }
                return ResponseEntity.ok(MAPPER.writeValueAsString(serviceRegistry.findServiceByExactServiceId(id)));
            }

            @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
            @SneakyThrows
            public ResponseEntity load() {
                return ResponseEntity.ok(MAPPER.writeValueAsString(serviceRegistry.load()));
            }
        }
    }

}
