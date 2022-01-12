package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.wsfederation.web.WsFederationNavigationController;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WsFederationClientRedirectActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SpringBootTest(classes = BaseWsFederationWebflowTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.wsfed[0].identity-provider-url=https://example.org/adfs/ls/",
        "cas.authn.wsfed[0].identity-provider-identifier=https://example.org/adfs/services/trust",
        "cas.authn.wsfed[0].relying-party-identifier=urn:cas:example",
        "cas.authn.wsfed[0].signing-certificate-resources=classpath:adfs-signing.crt",
        "cas.authn.wsfed[0].identity-attribute=upn",
        "cas.authn.wsfed[0].auto-redirect-type=SERVER",
        "server.servlet.context-path=/cas"
    })
@Tag("WebflowActions")
@EnableConfigurationProperties({CasConfigurationProperties.class, ServerProperties.class})
public class WsFederationClientRedirectActionTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_WS_FEDERATION_REDIRECT)
    protected Action wsFederationRedirectAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_WS_FEDERATION)
    protected Action wsFederationAction;

    @Autowired
    private ServerProperties serverProperties;

    @Test
    public void verifyRequestOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        wsFederationAction.execute(context);
        wsFederationRedirectAction.execute(context);
        assertEquals(HttpStatus.FOUND.value(), response.getStatus());
        assertTrue(response.getHeader("Location").startsWith(
            serverProperties.getServlet().getContextPath() + WsFederationNavigationController.ENDPOINT_REDIRECT));
    }
}
