package org.apereo.cas.consent;

import org.apereo.cas.config.CasConsentRestAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link RestfulConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("RestfulApi")
@SpringBootTest(classes = {
    RestfulConsentRepositoryTests.RestConsentRepositoryTestConfiguration.class,
    CasConsentRestAutoConfiguration.class,
    BaseConsentRepositoryTests.SharedTestConfiguration.class
}, properties = "cas.consent.rest.url=http://localhost:${#applicationContext.get().environment.getProperty('local.server.port')}/consent",
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Getter
@ExtendWith(CasTestExtension.class)
class RestfulConsentRepositoryTests extends BaseConsentRepositoryTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier("restConsentRepositoryStorage")
    protected RestConsentRepositoryStorage storage;

    @Autowired
    @Qualifier(ConsentRepository.BEAN_NAME)
    protected ConsentRepository repository;

    @BeforeEach
    void initialize() {
        SpringExpressionLanguageValueResolver.getInstance().withApplicationContext(applicationContext);
        storage.getRecords().clear();
    }

    @Getter
    private static final class RestConsentRepositoryStorage {
        private final Map<String, List<ConsentDecision>> records = new HashMap<>();
    }

    @TestConfiguration(value = "RestConsentRepositoryTestConfiguration", proxyBeanMethods = false)
    static class RestConsentRepositoryTestConfiguration {

        @Bean
        public RestConsentRepositoryStorage restConsentRepositoryStorage() {
            return new RestConsentRepositoryStorage();
        }

        @Bean
        public CasWebSecurityConfigurer<Void> consentEndpointConfigurer() {
            return new CasWebSecurityConfigurer<>() {
                @Override
                public List<String> getIgnoredEndpoints() {
                    return List.of("/consent");
                }
            };
        }

        @RestController("consentController")
        @RequestMapping("/consent")
        static class ConsentController {

            @Autowired
            @Qualifier("restConsentRepositoryStorage")
            private RestConsentRepositoryStorage storage;

            @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
            public ResponseEntity<String> find(@RequestHeader(value = "principal", required = false) final String principal,
                                               @RequestHeader(value = "service", required = false) final String service) throws Exception {
                val consentDecisions = storage.getRecords().get(principal);
                if (consentDecisions != null && StringUtils.isNotBlank(principal) && StringUtils.isNotBlank(service)) {
                    val results = consentDecisions
                        .stream()
                        .filter(consentDecision -> consentDecision.getService().equals(service))
                        .findFirst();

                    if (results.isPresent()) {
                        return ResponseEntity.ok(MAPPER.writeValueAsString(results.get()));
                    }
                    return ResponseEntity.notFound().build();
                }

                if (StringUtils.isNotBlank(principal)) {
                    return ResponseEntity.ok(MAPPER.writeValueAsString(consentDecisions));
                }
                val results = storage.getRecords().values()
                    .stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
                return ResponseEntity.ok(MAPPER.writeValueAsString(results));
            }

            @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
            public ResponseEntity store(@RequestBody final ConsentDecision decision) {
                if (storage.getRecords().containsKey(decision.getPrincipal())) {
                    val decisions = storage.getRecords().get(decision.getPrincipal());
                    decisions.removeIf(consentDecision -> consentDecision.getId() == decision.getId());
                    decisions.add(decision);
                } else {
                    storage.getRecords().put(decision.getPrincipal(), CollectionUtils.wrapList(decision));
                }
                return ResponseEntity.ok().build();
            }

            @DeleteMapping(path = "/{decisionId}", produces = MediaType.APPLICATION_JSON_VALUE)
            public ResponseEntity delete(@RequestHeader("principal") final String principal,
                                         @PathVariable final long decisionId) {
                if (storage.getRecords().containsKey(principal)) {
                    val decisions = storage.getRecords().get(principal);
                    decisions.removeIf(d -> d.getId() == decisionId);
                    return ResponseEntity.ok().build();
                }
                return ResponseEntity.notFound().build();
            }

            @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
            public ResponseEntity deleteForPrincipal(@RequestHeader("principal") final String principal) {
                if (storage.getRecords().containsKey(principal)) {
                    storage.getRecords().remove(principal);
                    return ResponseEntity.ok().build();
                }
                return ResponseEntity.notFound().build();
            }
        }
    }
}
