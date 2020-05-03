package org.apereo.cas.adaptors.u2f;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.adaptors.u2f.web.flow.BaseU2FWebflowActionTests;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link U2FAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = BaseU2FWebflowActionTests.SharedTestConfiguration.class,
    properties = "cas.authn.mfa.u2f.json.location=file:src/test/resources/u2f-accounts.json"
)
@DirtiesContext
@Tag("MFA")
public class U2FAuthenticationHandlerTests {
    @Autowired
    @Qualifier("u2fAuthenticationHandler")
    private AuthenticationHandler u2fAuthenticationHandler;

    @Autowired
    @Qualifier("u2fDeviceRepository")
    private U2FDeviceRepository u2fDeviceRepository;

    @Test
    public void verifyOperation() throws Exception {
        val token = '{'
            + "\"keyHandle\":\"2_QYgDSPYcOgYBGBe8c9PVCunjigbD-3o5HcliXhu-Up_GKckYMxxVF6AgSPWubqfWy8WmJNDYQEJ1QKZe343Q\","
            + "\"clientData\":\"eyJ0eXAiOiJuYXZpZ2F0b3IuaWQuZ2V0QXNzZXJ0aW9uIiwiY2hhbGxlbmdlIjoiTkVuQUVaUE9vU1R2R"
            + "DMzY3JUZWQ4WUVOaXp2V1o1bXVGWllmZllwM0FlVSIsIm9yaWdpbiI6Imh0dHBzOi8vbW1vYXl5ZWQudW5pY29uLm5ldDo4NDQzIiwiY2lkX3B1YmtleSI6InVudXNlZCJ9\","
            + "\"signatureData\":\"AQAAABQwRgIhAJ_VcJ7WFDyaW2rf2fXVqpmh7nV9G8fULDiX9cHEdjZjAiEA0zJ2_dFS42wYi062yhEYyqDnA3mDX3PKvFzo7EorZs0\""
            + '}';

        val authnData = "{\"appId\":\"https://mmoayyed.unicon.net:8443\","
            + "\"challenge\":\"NEnAEZPOoSTvD33crTPasswordlessAuthenticationWebflowConfigurerTestsed8YENizvWZ5muFZYffYp3AeU\",\"signRequests\":[{\"version\":\"U2F_V2\","
            + "\"challenge\":\"NEnAEZPOoSTvD33crTed8YENizvWZ5muFZYffYp3AeU\",\"appId\":\"https://mmoayyed.unicon.net:8443\","
            + "\"keyHandle\":\"2_QYgDSPYcOgYBGBe8c9PVCunjigbD-3o5HcliXhu-Up_GKckYMxxVF6AgSPWubqfWy8WmJNDYQEJ1QKZe343Q\"}]}";
        u2fDeviceRepository.requestDeviceAuthentication("NEnAEZPOoSTvD33crTed8YENizvWZ5muFZYffYp3AeU", "casuser", authnData);

        val credential = new U2FTokenCredential(token);
        assertTrue(u2fAuthenticationHandler.supports(credential));

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        RequestContextHolder.setRequestContext(context);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);
        val result = u2fAuthenticationHandler.authenticate(credential);
        assertNotNull(result);
    }
}
