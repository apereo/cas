package org.apereo.cas.mfa.simple.web;

import module java.base;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.mfa.simple.BaseCasSimpleMultifactorAuthenticationTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.report.AbstractCasEndpointTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSimpleMultifactorAuthenticationEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("ActuatorEndpoint")
@TestPropertySource(properties = "management.endpoint.mfaSimple.access=UNRESTRICTED")
@Import({
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class
})
class CasSimpleMultifactorAuthenticationEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("mfaSimpleMultifactorEndpoint")
    private CasSimpleMultifactorAuthenticationEndpoint endpoint;

    @Test
    void verifyGenerateToken() {
        val authorization = EncodingUtils.encodeBase64("casuser:casuser");
        val results = endpoint.generateToken(RegisteredServiceTestUtils.CONST_TEST_URL, authorization);
        assertTrue(results.getStatusCode().is2xxSuccessful());
    }

    @Test
    void verifyAuthFails() {
        val authorization = EncodingUtils.encodeBase64("casuser:unknown");
        val results = endpoint.generateToken(RegisteredServiceTestUtils.CONST_TEST_URL, authorization);
        assertTrue(results.getStatusCode().isError());
    }
}
