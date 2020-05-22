package org.apereo.cas.mfa.accepto;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AccepttoMultifactorAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("MFA")
@SpringBootTest(classes = BaseAccepttoMultifactorAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.acceptto.apiUrl=http://localhost:5002",
        "cas.authn.mfa.acceptto.application-id=thisisatestid",
        "cas.authn.mfa.acceptto.secret=thisisasecret",
        "cas.authn.mfa.acceptto.organization-id=thisisatestid",
        "cas.authn.mfa.acceptto.organization-secret=thisisasecret",
        "cas.authn.mfa.acceptto.registration-api-public-key.location=classpath:publickey.pem",

        "cas.authn.mfa.acceptto.bypass.principal-attribute-name=nothing",
        "cas.authn.mfa.acceptto.bypass.authentication-attribute-name=nothing",
        "cas.authn.mfa.acceptto.bypass.credential-class-type=UsernamePasswordCredential",
        "cas.authn.mfa.acceptto.bypass.http-request-remote-address=1.2.3.4",
        "cas.authn.mfa.acceptto.bypass.groovy.location=classpath:GroovyBypass.groovy",
        "cas.authn.mfa.acceptto.bypass.rest.url=http://localhost:8080/bypass"
    })
public class AccepttoMultifactorAuthenticationHandlerTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyOperationApproved() throws Exception {
        val data = MAPPER.writeValueAsString(CollectionUtils.wrap("device_id", "deviceid-test", "status", "approved"));
        try (val webServer = new MockWebServer(5002,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val handler = buildHandler();

            val credential = new AccepttoMultifactorTokenCredential("test-channel");
            assertTrue(handler.supports(credential));
            assertTrue(handler.supports(AccepttoMultifactorTokenCredential.class));
            val result = handler.authenticate(credential);
            assertNotNull(result.getPrincipal());
        }
    }

    private AccepttoMultifactorAuthenticationHandler buildHandler() {
        val handler = new AccepttoMultifactorAuthenticationHandler(mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), casProperties.getAuthn().getMfa().getAcceptto());
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);
        RequestContextHolder.setRequestContext(context);
        return handler;
    }
}
