package org.apereo.cas.services;

import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRegisteredServiceAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RegisteredService")
class DefaultRegisteredServiceAuthenticationPolicyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static void verify(final RegisteredServiceAuthenticationPolicyCriteria criteria) throws Exception {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        var svc = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setRequiredAuthenticationHandlers(Set.of("handler1", "handler2"));
        policy.setCriteria(criteria);
        svc.setAuthenticationPolicy(policy);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(jsonFile, svc);
        val svc2 = MAPPER.readValue(jsonFile, BaseRegisteredService.class);
        assertEquals(svc, svc2);
    }

    @Test
    void verifyAnySerializeToJson() throws Throwable {
        val criteria = new AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria();
        criteria.setTryAll(true);
        verify(criteria);
    }

    @Test
    void verifyAllSerializeToJson() throws Throwable {
        val criteria = new AllAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria();
        verify(criteria);
    }

    @Test
    void verifyGroovySerializeToJson() throws Throwable {
        val criteria = new GroovyRegisteredServiceAuthenticationPolicyCriteria();
        criteria.setScript("groovy { return Optional.empty() }");
        verify(criteria);
    }

    @Test
    void verifyRestfulSerializeToJson() throws Throwable {
        val criteria = new RestfulRegisteredServiceAuthenticationPolicyCriteria();
        criteria.setUrl("https://example.org");
        verify(criteria);
    }
}
