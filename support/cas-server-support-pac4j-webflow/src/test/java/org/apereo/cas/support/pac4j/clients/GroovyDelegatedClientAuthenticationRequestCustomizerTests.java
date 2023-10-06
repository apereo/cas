package org.apereo.cas.support.pac4j.clients;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.context.WebContext;
import org.pac4j.jee.context.JEEContext;
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
import static org.springframework.core.Ordered.*;

/**
 * This is {@link GroovyDelegatedClientAuthenticationRequestCustomizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Groovy")
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.authn.pac4j.core.groovy-authentication-request-customizer.location=classpath:/AuthnRequestCustomizer.groovy")
class GroovyDelegatedClientAuthenticationRequestCustomizerTests {
    protected WebContext context;

    protected MockRequestContext requestContext;

    protected MockHttpServletRequest httpServletRequest;

    protected MockHttpServletResponse httpServletResponse;

    @Autowired
    @Qualifier("groovyDelegatedClientAuthenticationRequestCustomizer")
    private DelegatedClientAuthenticationRequestCustomizer groovyDelegatedClientAuthenticationRequestCustomizer;

    @BeforeEach
    public void setup() {
        val service = RegisteredServiceTestUtils.getService();
        httpServletResponse = new MockHttpServletResponse();
        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        context = new JEEContext(httpServletRequest, httpServletResponse);

        requestContext = new MockRequestContext();
        requestContext.setExternalContext(new ServletExternalContext(new MockServletContext(),
            httpServletRequest, httpServletResponse));
        RequestContextHolder.setRequestContext(requestContext);
        ExternalContextHolder.setExternalContext(requestContext.getExternalContext());
    }

    @Test
    void verifyOperation() throws Throwable {
        val client = new CasClient(new CasConfiguration("https://example.org/cas/login"));
        client.setCallbackUrl("https://example.org/cas/callback");
        client.init();
        groovyDelegatedClientAuthenticationRequestCustomizer.customize(client, context);
        assertTrue(context.getRequestAttribute("customAttribute").isPresent());
        assertTrue(groovyDelegatedClientAuthenticationRequestCustomizer.supports(client, context));
        assertTrue(groovyDelegatedClientAuthenticationRequestCustomizer.isAuthorized(context,
            client, RegisteredServiceTestUtils.getService()));
        assertEquals(HIGHEST_PRECEDENCE, groovyDelegatedClientAuthenticationRequestCustomizer.getOrder());
    }
}
