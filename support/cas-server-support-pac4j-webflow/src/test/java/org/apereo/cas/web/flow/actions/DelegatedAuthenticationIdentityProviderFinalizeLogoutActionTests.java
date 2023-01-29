package org.apereo.cas.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedAuthenticationIdentityProviderFinalizeLogoutActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Delegation")
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
public class DelegatedAuthenticationIdentityProviderFinalizeLogoutActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_IDP_FINALIZE_LOGOUT)
    private Action action;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, "SAML2Client");
        request.addParameter(casProperties.getLogout().getRedirectParameter().get(0), "https://github.com");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "Mozilla/5.0 (Windows NT 10.0; WOW64)");
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        assertNull(action.execute(context));
        assertNotNull(WebUtils.getLogoutRedirectUrl(request, String.class));
    }
}
