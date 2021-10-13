package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultDelegatedClientIdentityProviderConfigurationProducerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Delegation")
public class DefaultDelegatedClientIdentityProviderConfigurationProducerTests {

    @SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class,
        properties = "cas.authn.pac4j.cookie.enabled=true")
    @SuppressWarnings("ClassCanBeStatic")
    public abstract class BaseDelegatedClientIdentityProviderConfigurationProducerTests {
        @Autowired
        @Qualifier(DelegatedClientIdentityProviderConfigurationProducer.BEAN_NAME)
        protected DelegatedClientIdentityProviderConfigurationProducer delegatedClientIdentityProviderConfigurationProducer;

        @Autowired
        @Qualifier("delegatedAuthenticationCookieGenerator")
        protected CasCookieBuilder delegatedAuthenticationCookieGenerator;

        protected JEEContext context;

        protected MockRequestContext requestContext;

        protected MockHttpServletRequest httpServletRequest;

        protected MockHttpServletResponse httpServletResponse;

        @BeforeEach
        public void setup() {
            val service = RegisteredServiceTestUtils.getService();
            httpServletResponse = new MockHttpServletResponse();
            httpServletRequest = new MockHttpServletRequest();
            httpServletRequest.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            context = new JEEContext(httpServletRequest, httpServletResponse);

            requestContext = new MockRequestContext();
            requestContext.setExternalContext(new ServletExternalContext(new MockServletContext(),
                context.getNativeRequest(), context.getNativeResponse()));
            RequestContextHolder.setRequestContext(requestContext);
            ExternalContextHolder.setExternalContext(requestContext.getExternalContext());
        }

        @Test
        public void verifyOperation() {
            delegatedAuthenticationCookieGenerator.addCookie(context.getNativeRequest(),
                context.getNativeResponse(), "SAML2Client");
            val results = delegatedClientIdentityProviderConfigurationProducer.produce(requestContext);
            assertFalse(results.isEmpty());
            assertNotNull(WebUtils.getDelegatedAuthenticationProviderPrimary(requestContext));
        }

        @Test
        public void verifyProduceFailingClient() {
            delegatedAuthenticationCookieGenerator.addCookie(context.getNativeRequest(),
                context.getNativeResponse(), "FailingClient");
            val results = delegatedClientIdentityProviderConfigurationProducer.produce(requestContext);
            assertFalse(results.isEmpty());
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class MenuSelectionTests extends BaseDelegatedClientIdentityProviderConfigurationProducerTests {
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = "cas.authn.pac4j.core.discovery-selection.selection-type=DYNAMIC")
    public class DynamicSelectionTests extends BaseDelegatedClientIdentityProviderConfigurationProducerTests {
        @Test
        public void verifySelectionOperation() {
            val results = delegatedClientIdentityProviderConfigurationProducer.produce(requestContext);
            assertFalse(results.isEmpty());
            assertTrue(WebUtils.isDelegatedAuthenticationDynamicProviderSelection(requestContext));
        }
    }
}
