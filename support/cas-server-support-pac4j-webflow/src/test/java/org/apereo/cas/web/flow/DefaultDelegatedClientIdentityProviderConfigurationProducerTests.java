package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.http.OkAction;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultDelegatedClientIdentityProviderConfigurationProducerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Delegation")
class DefaultDelegatedClientIdentityProviderConfigurationProducerTests {

    /**
     * A pac4j client asked to initialize while another thread is already initializing the same
     * instance returns at once, without initializing and without waiting. Taken at face value that
     * reads as a provider that cannot be used, and because the producer swallows every per-provider
     * failure it would quietly hand back no identity providers at all. The second caller must
     * instead wait for the initialization in flight and find the provider ready.
     *
     * @throws Throwable in case of failure
     */
    @Test
    void verifyConcurrentIdentityProviderInitialization() throws Throwable {
        val initializing = new CountDownLatch(1);
        val release = new CountDownLatch(1);
        val client = new IndirectClient() {
            @Override
            protected void internalInit(final boolean force) {
                initializing.countDown();
                FunctionUtils.doUnchecked(_ -> release.await());
            }
        };
        client.setCredentialsExtractor(callContext -> Optional.empty());
        client.setRedirectionActionBuilder(callContext -> Optional.of(new OkAction("http://localhost")));
        client.setAuthenticator(mock(Authenticator.class));
        client.setProfileCreator((callContext, store) -> Optional.empty());
        client.setCallbackUrl("http://localhost/cas/login");
        client.setName("ConcurrentInitClient");

        val producer = new DefaultDelegatedClientIdentityProviderConfigurationProducer(mock(ObjectProvider.class));
        val context = MockRequestContext.create();
        try (val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            val first = executor.submit(() -> {
                FunctionUtils.doUnchecked(_ -> producer.initializeClientIdentityProvider(client, context));
                return Boolean.TRUE;
            });
            assertTrue(initializing.await(30, TimeUnit.SECONDS));
            val second = executor.submit(() -> {
                FunctionUtils.doUnchecked(_ -> producer.initializeClientIdentityProvider(client, context));
                return Boolean.TRUE;
            });
            release.countDown();
            assertEquals(Boolean.TRUE, first.get(30, TimeUnit.SECONDS));
            assertEquals(Boolean.TRUE, second.get(30, TimeUnit.SECONDS));
        }
        assertTrue(client.isInitialized());
    }

    @SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class, properties = "cas.authn.pac4j.cookie.enabled=true")
    @ExtendWith(CasTestExtension.class)
    public abstract static class BaseDelegatedClientIdentityProviderConfigurationProducerTests {
        @Autowired
        @Qualifier(DelegatedClientIdentityProviderConfigurationProducer.BEAN_NAME)
        protected DelegatedClientIdentityProviderConfigurationProducer delegatedClientIdentityProviderConfigurationProducer;

        @Autowired
        @Qualifier("delegatedAuthenticationCookieGenerator")
        protected CasCookieBuilder delegatedAuthenticationCookieGenerator;

        @Autowired
        @Qualifier("delegatedAuthenticationCasWebflowLoginContextProvider")
        protected CasWebflowLoginContextProvider delegatedAuthenticationCasWebflowLoginContextProvider;

        @Autowired
        protected ConfigurableApplicationContext applicationContext;

        protected JEEContext context;

        protected MockRequestContext requestContext;

        protected MockHttpServletRequest httpServletRequest;

        protected MockHttpServletResponse httpServletResponse;

        @BeforeEach
        void setup() throws Exception {
            requestContext = MockRequestContext.create(applicationContext);
            httpServletResponse = requestContext.getHttpServletResponse();
            httpServletRequest = requestContext.getHttpServletRequest();
            val service = RegisteredServiceTestUtils.getService();
            requestContext.setParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            context = new JEEContext(httpServletRequest, httpServletResponse);
        }

        @Test
        void verifyOperation() throws Throwable {
            delegatedAuthenticationCookieGenerator.addCookie(context.getNativeRequest(),
                context.getNativeResponse(), "CasClient");
            val results = delegatedClientIdentityProviderConfigurationProducer.produce(requestContext);
            assertFalse(results.isEmpty());
            assertNotNull(DelegationWebflowUtils.getDelegatedAuthenticationProviderPrimary(requestContext));
            assertFalse(delegatedAuthenticationCasWebflowLoginContextProvider.isLoginFormViewable(requestContext));
        }

        @Test
        void verifyProduceFailingClient() throws Throwable {
            delegatedAuthenticationCookieGenerator.addCookie(context.getNativeRequest(),
                context.getNativeResponse(), "FailingClient");
            val results = delegatedClientIdentityProviderConfigurationProducer.produce(requestContext);
            assertFalse(results.isEmpty());
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.pac4j.core.discovery-selection.selection-type=MENU")
    class MenuSelectionTests extends BaseDelegatedClientIdentityProviderConfigurationProducerTests {
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.pac4j.core.discovery-selection.selection-type=DYNAMIC")
    class DynamicSelectionTests extends BaseDelegatedClientIdentityProviderConfigurationProducerTests {
        @Test
        void verifySelectionOperation() throws Throwable {
            val results = delegatedClientIdentityProviderConfigurationProducer.produce(requestContext);
            assertFalse(results.isEmpty());
            assertTrue(DelegationWebflowUtils.isDelegatedAuthenticationDynamicProviderSelection(requestContext));
        }
    }
}
