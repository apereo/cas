package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mfa.accepto.BaseAccepttoMultifactorAuthenticationTests;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

/**
 * This is {@link AccepttoMultifactorValidateChannelActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("RestfulApi")
@SpringBootTest(classes = BaseAccepttoMultifactorAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.acceptto.api-url=http://localhost:5001",
        "cas.authn.mfa.acceptto.application-id=thisisatestid",
        "cas.authn.mfa.acceptto.secret=thisisasecret",
        "cas.authn.mfa.acceptto.organization-id=thisisatestid",
        "cas.authn.mfa.acceptto.organization-secret=thisisasecret",
        "cas.authn.mfa.acceptto.registration-api-public-key.location=classpath:publickey.pem"
    })
public class AccepttoMultifactorValidateChannelActionTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("mfaAccepttoDistributedSessionStore")
    private SessionStore mfaAccepttoDistributedSessionStore;

    @Test
    public void verifyOperation() throws Exception {
        val httpRequest = new MockHttpServletRequest();
        httpRequest.setRemoteAddr("185.86.151.11");
        httpRequest.setLocalAddr("185.88.151.11");
        ClientInfoHolder.setClientInfo(new ClientInfo(httpRequest));

        val data = MAPPER.writeValueAsString(CollectionUtils.wrap("channel", "test-channel",
            "status", "approved", "device_id", "deviceid-123456"));
        try (val webServer = new MockWebServer(5001,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val action = new AccepttoMultifactorValidateChannelAction(mfaAccepttoDistributedSessionStore, authenticationSystemSupport);
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            val webContext = new JEEContext(request, response);
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            val authn = CoreAuthenticationTestUtils.getAuthentication("casuser");
            WebUtils.putAuthentication(authn, context);
            AccepttoWebflowUtils.storeChannelInSessionStore("test-channel", webContext, mfaAccepttoDistributedSessionStore);
            AccepttoWebflowUtils.storeAuthenticationInSessionStore(authn, webContext, mfaAccepttoDistributedSessionStore);
            RequestContextHolder.setRequestContext(context);
            val result = action.doExecute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINALIZE, result.getId());
        }
    }
}
