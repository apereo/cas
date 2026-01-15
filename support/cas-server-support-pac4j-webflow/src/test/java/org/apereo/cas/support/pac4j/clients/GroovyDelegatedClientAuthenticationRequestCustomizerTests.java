package org.apereo.cas.support.pac4j.clients;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.context.WebContext;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/**
 * This is {@link GroovyDelegatedClientAuthenticationRequestCustomizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Groovy")
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.authn.pac4j.core.groovy-authentication-request-customizer.location=classpath:/AuthnRequestCustomizer.groovy")
@ExtendWith(CasTestExtension.class)
class GroovyDelegatedClientAuthenticationRequestCustomizerTests {
    protected WebContext context;

    protected MockRequestContext requestContext;

    protected MockHttpServletRequest httpServletRequest;

    protected MockHttpServletResponse httpServletResponse;

    @Autowired
    @Qualifier("groovyDelegatedClientAuthenticationRequestCustomizer")
    private DelegatedClientAuthenticationRequestCustomizer groovyDelegatedClientAuthenticationRequestCustomizer;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @BeforeEach
    void setup() throws Exception {
        val service = RegisteredServiceTestUtils.getService();
        requestContext = MockRequestContext.create(applicationContext);
        httpServletResponse = requestContext.getHttpServletResponse();
        httpServletRequest = requestContext.getHttpServletRequest();
        httpServletRequest.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        context = new JEEContext(httpServletRequest, httpServletResponse);
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
