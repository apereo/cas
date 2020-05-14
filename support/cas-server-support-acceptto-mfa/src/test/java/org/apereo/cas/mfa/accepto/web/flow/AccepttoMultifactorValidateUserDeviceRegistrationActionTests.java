package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.accepto.AccepttoEmailCredential;
import org.apereo.cas.mfa.accepto.BaseAccepttoMultifactorAuthenticationTests;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccepttoMultifactorValidateUserDeviceRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RestfulApi")
@SpringBootTest(classes = BaseAccepttoMultifactorAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.acceptto.apiUrl=http://localhost:5011",
        "cas.authn.mfa.acceptto.application-id=thisisatestid",
        "cas.authn.mfa.acceptto.group-attribute=group",
        "cas.authn.mfa.acceptto.email-attribute=email",
        "cas.authn.mfa.acceptto.secret=thisisasecret",
        "cas.authn.mfa.acceptto.organization-id=thisisatestid",
        "cas.authn.mfa.acceptto.organization-secret=thisisasecret",
        "cas.authn.mfa.acceptto.registration-api-public-key.location=classpath:publickey.pem"
    })
public class AccepttoMultifactorValidateUserDeviceRegistrationActionTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyOperation() throws Exception {
        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of(
            "email", List.of("cas@example.org"),
            "group", List.of("staff")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);

        val data = MAPPER.writeValueAsString(Map.of("device_paired", "true"));
        try (val webServer = new MockWebServer(5011,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
           
            WebUtils.putAuthentication(authentication, context);
            RequestContextHolder.setRequestContext(context);

            val action = new AccepttoMultifactorValidateUserDeviceRegistrationAction(casProperties);
            val result = action.doExecute(context);
            assertEquals(result.getId(), CasWebflowConstants.TRANSITION_ID_FINALIZE);
            assertTrue(WebUtils.getCredential(context) instanceof AccepttoEmailCredential);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }

    }
}
