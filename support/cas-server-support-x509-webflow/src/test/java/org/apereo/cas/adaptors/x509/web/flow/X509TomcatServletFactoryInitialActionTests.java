package org.apereo.cas.adaptors.x509.web.flow;

import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.X509TomcatServletFactoryInitialAction;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link X509TomcatServletFactoryInitialActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowActions")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class
},
    properties = "cas.authn.x509.webflow.port=9876")
public class X509TomcatServletFactoryInitialActionTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyOperation() throws Exception {
        val action = new X509TomcatServletFactoryInitialAction(casProperties);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val result = action.execute(context);
        assertNull(result);

        assertNotNull(context.getFlowScope().get(X509TomcatServletFactoryInitialAction.ATTRIBUTE_X509_CLIENT_AUTH_LOGIN_ENDPOINT_URL));
    }

}
