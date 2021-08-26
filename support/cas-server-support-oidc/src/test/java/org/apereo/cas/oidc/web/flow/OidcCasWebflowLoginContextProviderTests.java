package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.web.flow.CasWebflowLoginContextProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcCasWebflowLoginContextProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OIDC")
public class OidcCasWebflowLoginContextProviderTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcCasWebflowLoginContextProvider")
    private CasWebflowLoginContextProvider oidcCasWebflowLoginContextProvider;

    @Test
    public void verifyOperation() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        assertTrue(oidcCasWebflowLoginContextProvider.getCandidateUsername(context).isEmpty());
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, "https://localhost/cas?service=https://example.net&login_hint=casuser");
        assertTrue(oidcCasWebflowLoginContextProvider.getCandidateUsername(context).isPresent());
    }
}
