package org.apereo.cas.mfa.simple.rest;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.BaseCasSimpleMultifactorAuthenticationTests;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSimpleMultifactorRestHttpRequestCredentialFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("MFAProvider")
public class CasSimpleMultifactorRestHttpRequestCredentialFactoryTests {
    @Autowired
    @Qualifier("casSimpleMultifactorRestHttpRequestCredentialFactory")
    private RestHttpRequestCredentialFactory casSimpleMultifactorRestHttpRequestCredentialFactory;

    @Test
    public void verifyAction() {
        val body = new LinkedMultiValueMap<String, String>();
        body.add(CasSimpleMultifactorRestHttpRequestCredentialFactory.PARAMETER_NAME_CAS_SIMPLE_OTP, "token");
        assertFalse(casSimpleMultifactorRestHttpRequestCredentialFactory.fromRequest(null, body).isEmpty());
    }

    @Test
    public void verifyEmptyBody() {
        val body = new LinkedMultiValueMap<String, String>();
        assertTrue(casSimpleMultifactorRestHttpRequestCredentialFactory.fromRequest(null, body).isEmpty());
        body.put("some-other-key", List.of("value1"));
        assertTrue(casSimpleMultifactorRestHttpRequestCredentialFactory.fromRequest(null, body).isEmpty());
    }
}
