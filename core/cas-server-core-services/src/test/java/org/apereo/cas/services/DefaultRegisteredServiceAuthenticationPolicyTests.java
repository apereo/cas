package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRegisteredServiceAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class DefaultRegisteredServiceAuthenticationPolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "ServiceAuthenticationPolicy.json");

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifySerializeToJson() throws IOException {
        var svc = RegisteredServiceTestUtils.getRegisteredService("serviceidauth");
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setRequiredAuthenticationHandlers(Set.of("handler1", "handler2"));
        val criteria = new DefaultRegisteredServiceAuthenticationPolicyCriteria();
        criteria.setTryAll(true);
        criteria.setType(RegisteredServiceAuthenticationPolicyCriteria.AuthenticationPolicyTypes.ANY_AUTHENTICATION_HANDLER);
        policy.setCriteria(criteria);
        svc.setAuthenticationPolicy(policy);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(JSON_FILE, svc);
        val svc2 = MAPPER.readValue(JSON_FILE, AbstractRegisteredService.class);
        assertEquals(svc, svc2);
    }
}
