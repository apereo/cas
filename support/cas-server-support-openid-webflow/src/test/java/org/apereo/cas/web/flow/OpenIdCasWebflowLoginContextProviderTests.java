package org.apereo.cas.web.flow;

import org.apereo.cas.support.openid.AbstractOpenIdTests;
import org.apereo.cas.web.flow.config.OpenIdWebflowConfiguration;
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
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OpenIdCasWebflowLoginContextProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 * @deprecated 6.2.0
 */
@Tag("WebflowAuthenticationActions")
@Deprecated(since = "6.2.0")
@SpringBootTest(classes = {
    AbstractOpenIdTests.SharedTestConfiguration.class,
    OpenIdWebflowConfiguration.class
})
public class OpenIdCasWebflowLoginContextProviderTests {
    @Autowired
    @Qualifier("openidCasWebflowLoginContextProvider")
    private CasWebflowLoginContextProvider openidCasWebflowLoginContextProvider;

    @Test
    public void verifyOperation() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        WebUtils.putOpenIdLocalUserId(context, "casuser");
        val results = openidCasWebflowLoginContextProvider.getCandidateUsername(context);
        assertFalse(results.isEmpty());
        assertEquals("casuser", results.get());
    }
}
