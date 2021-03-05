package org.apereo.cas.web.flow;

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
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WsFederationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseWsFederationWebflowTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.wsfed[0].identity-provider-url=https://example.org/adfs/ls/",
        "cas.authn.wsfed[0].identity-provider-identifier=https://example.org/adfs/services/trust",
        "cas.authn.wsfed[0].relying-party-identifier=urn:cas:example",
        "cas.authn.wsfed[0].signing-certificate-resources=classpath:adfs-signing.crt",
        "cas.authn.wsfed[0].identity-attribute=upn"
    })
@Tag("WebflowActions")
public class WsFederationActionTests {
    @Autowired
    @Qualifier("wsFederationAction")
    protected Action wsFederationAction;

    @Test
    public void verifyRequestOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        wsFederationAction.execute(context);
        assertTrue(context.getFlowScope().contains(WsFederationRequestBuilder.PARAMETER_NAME_WSFED_CLIENTS));
    }
}
