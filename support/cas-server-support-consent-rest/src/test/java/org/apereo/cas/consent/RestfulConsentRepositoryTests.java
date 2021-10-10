package org.apereo.cas.consent;

import org.apereo.cas.config.CasConsentRestConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
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
    CasConsentRestConfiguration.class,
    BaseConsentRepositoryTests.SharedTestConfiguration.class
}, properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "server.port=8733",
    "cas.consent.rest.url=http://localhost:8733"
}, 
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Getter
@EnableAutoConfiguration
public class RestfulConsentRepositoryTests extends BaseConsentRepositoryTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier("restConsentRepositoryStorage")
    protected RestConsentRepositoryStorage storage;

    @Autowired
    @Qualifier("consentRepository")
    protected ConsentRepository repository;

    @BeforeEach
    public void initialize() {
        storage.getRecords().clear();
    }

    @Getter
    private static class RestConsentRepositoryStorage {
        private final Map<String, List<ConsentDecision>> records = new HashMap<>();
    }

    @TestConfiguration
    @Lazy(false)
    public static class RestConsentRepositoryTestConfiguration {

        @Bean
        public RestConsentRepositoryStorage restConsentRepositoryStorage() {
            return new RestConsentRepositoryStorage();
        }

        @RestController("consentController")
        @RequestMapping("/")
        public static class ConsentController {

            @Autowired
            @Qualifier("restConsentRepositoryStorage")
            private RestConsentRepositoryStorage storage;

            @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
            @SneakyThrows
            public ResponseEntity<String> find(@RequestHeader(value = "principal", required = false) final String principal,
                                       @RequestHeader(value = "service", required = false) final String service) {
                if (StringUtils.isNotBlank(principal) && StringUtils.isNotBlank(service)) {
                    val results = storage.getRecords().get(principal)
                        .stream()
                        .filter(d -> d.getService().equals(service))
                        .findFirst();

                    if (results.isPresent()) {
                        return ResponseEntity.ok(MAPPER.writeValueAsString(results.get()));
                    }
                    return ResponseEntity.notFound().build();
                }

                if (StringUtils.isNotBlank(principal)) {
                    return ResponseEntity.ok(MAPPER.writeValueAsString(storage.getRecords().get(principal)));
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
                    decisions.removeIf(d -> d.getId() == decision.getId());
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
