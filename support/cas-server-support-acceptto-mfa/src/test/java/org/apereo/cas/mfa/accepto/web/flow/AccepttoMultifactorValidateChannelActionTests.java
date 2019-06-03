package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.AccepttoMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.AccepttoMultifactorAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.AccepttoMultifactorAuthenticationMultifactorProviderBypassConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
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
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    AccepttoMultifactorAuthenticationConfiguration.class,
    AccepttoMultifactorAuthenticationEventExecutionPlanConfiguration.class,
    AccepttoMultifactorAuthenticationMultifactorProviderBypassConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.mfa.acceptto.apiUrl=http://localhost:5001",
    "cas.authn.mfa.acceptto.application-id=thisisatestid",
    "cas.authn.mfa.acceptto.secret=thisisasecret",
    "cas.authn.mfa.acceptto.organization-id=thisisatestid",
    "cas.authn.mfa.acceptto.organization-secret=thisisasecret",
    "cas.authn.mfa.acceptto.registration-api-public-key.location=classpath:publickey.pem"
})
public class AccepttoMultifactorValidateChannelActionTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("mfaAccepttoDistributedSessionStore")
    private SessionStore<J2EContext> mfaAccepttoDistributedSessionStore;

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
            val webContext = new J2EContext(request, response, mfaAccepttoDistributedSessionStore);
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            val authn = CoreAuthenticationTestUtils.getAuthentication("casuser");
            WebUtils.putAuthentication(authn, context);
            AccepttoWebflowUtils.storeChannelInSessionStore("test-channel", webContext);
            AccepttoWebflowUtils.storeAuthenticationInSessionStore(authn, webContext);
            RequestContextHolder.setRequestContext(context);
            val result = action.doExecute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINALIZE, result.getId());
        }
    }
}
