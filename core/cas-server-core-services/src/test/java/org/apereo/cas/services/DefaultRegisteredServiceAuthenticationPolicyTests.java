package org.apereo.cas.services;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRegisteredServiceAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RegisteredService")
class DefaultRegisteredServiceAuthenticationPolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "ServiceAuthenticationPolicy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static void verify(final RegisteredServiceAuthenticationPolicyCriteria criteria) throws Exception {
        var svc = RegisteredServiceTestUtils.getRegisteredService("serviceidauth");
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setRequiredAuthenticationHandlers(Set.of("handler1", "handler2"));
        policy.setCriteria(criteria);
        svc.setAuthenticationPolicy(policy);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(JSON_FILE, svc);
        val svc2 = MAPPER.readValue(JSON_FILE, BaseRegisteredService.class);
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
    void verifyRestfulerializeToJson() throws Throwable {
        val criteria = new RestfulRegisteredServiceAuthenticationPolicyCriteria();
        criteria.setUrl("https://example.org");
        verify(criteria);
    }
}
