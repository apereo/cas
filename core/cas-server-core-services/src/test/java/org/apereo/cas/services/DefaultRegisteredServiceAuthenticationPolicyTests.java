package org.apereo.cas.services;

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
@Tag("Simple")
public class DefaultRegisteredServiceAuthenticationPolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "ServiceAuthenticationPolicy.json");

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static void verify(final RegisteredServiceAuthenticationPolicyCriteria criteria) throws Exception {
        var svc = RegisteredServiceTestUtils.getRegisteredService("serviceidauth");
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setRequiredAuthenticationHandlers(Set.of("handler1", "handler2"));
        policy.setCriteria(criteria);
        svc.setAuthenticationPolicy(policy);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(JSON_FILE, svc);
        val svc2 = MAPPER.readValue(JSON_FILE, AbstractRegisteredService.class);
        assertEquals(svc, svc2);
    }

    @Test
    public void verifyAnySerializeToJson() throws Exception {
        val criteria = new AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria();
        criteria.setTryAll(true);
        verify(criteria);
    }

    @Test
    public void verifyAllSerializeToJson() throws Exception {
        val criteria = new AllAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria();
        verify(criteria);
    }

    @Test
    public void verifyGroovySerializeToJson() throws Exception {
        val criteria = new GroovyRegisteredServiceAuthenticationPolicyCriteria();
        criteria.setScript("groovy { return Optional.empty() }");
        verify(criteria);
    }

    @Test
    public void verifyRestfulerializeToJson() throws Exception {
        val criteria = new RestfulRegisteredServiceAuthenticationPolicyCriteria();
        criteria.setUrl("https://example.org");
        verify(criteria);
    }
}
