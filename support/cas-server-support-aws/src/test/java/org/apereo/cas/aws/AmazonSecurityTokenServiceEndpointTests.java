package org.apereo.cas.aws;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.AmazonCoreConfiguration;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.rest.config.CasCoreRestConfiguration;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.report.AbstractCasEndpointTests;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AmazonSecurityTokenServiceEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@TestPropertySource(properties = {
    "cas.amazon-sts.endpoint=http://127.0.0.1:4566",
    "cas.amazon-sts.credential-access-key=test",
    "cas.amazon-sts.credential-secret-key=test",
    "cas.authn.mfa.groovy-script.location=classpath:AmazonStsGroovyMfa.groovy",
    "management.endpoint.awsSts.enabled=true"
})
@Tag("AmazonWebServices")
@Import({AmazonCoreConfiguration.class, CasCoreRestConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class})
public class AmazonSecurityTokenServiceEndpointTests extends AbstractCasEndpointTests {
    @Autowired
    @Qualifier("awsSecurityTokenServiceEndpoint")
    private AmazonSecurityTokenServiceEndpoint awsSecurityTokenServiceEndpoint;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @BeforeEach
    public void beforeEach() {
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
    }

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();

        val body = new LinkedMultiValueMap<String, String>();
        body.put("username", List.of("casuser"));
        body.put("password", List.of("resusac"));

        val credentials = awsSecurityTokenServiceEndpoint.fetchCredentials("PT15S", body, request);
        assertNotNull(credentials);
    }

    @Test
    public void verifyContextValidationFails() {
        val request = new MockHttpServletRequest();

        val body = new LinkedMultiValueMap<String, String>();
        body.put("username", List.of("test1234"));
        body.put("password", List.of("4321tset"));

        val credentials = awsSecurityTokenServiceEndpoint.fetchCredentials("PT15S", body, request);
        assertNotNull(credentials);
    }


    @Test
    public void verifyNoCredentials() {
        val request = new MockHttpServletRequest();
        val body = new LinkedMultiValueMap<String, String>();
        val status = awsSecurityTokenServiceEndpoint.fetchCredentials("PT15S", body, request);
        assertEquals(HttpStatus.BAD_REQUEST, status.getStatusCode());
    }

    @Test
    public void verifyFailsAuthN() {
        val request = new MockHttpServletRequest();

        val body = new LinkedMultiValueMap<String, String>();
        body.put("username", List.of("casuser"));
        body.put("password", List.of("bad-password"));

        val status = awsSecurityTokenServiceEndpoint.fetchCredentials("PT15S", body, request);
        assertEquals(HttpStatus.UNAUTHORIZED, status.getStatusCode());
    }
}
