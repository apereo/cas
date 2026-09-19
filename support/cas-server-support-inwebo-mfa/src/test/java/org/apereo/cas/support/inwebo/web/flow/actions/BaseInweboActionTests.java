package org.apereo.cas.support.inwebo.web.flow.actions;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationResult;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.support.inwebo.config.BaseInweboConfiguration;
import org.apereo.cas.support.inwebo.service.InweboService;
import org.apereo.cas.support.inwebo.service.response.InweboDeviceNameResponse;
import org.apereo.cas.support.inwebo.service.response.InweboResult;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is the base class for action tests.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    BaseInweboActionTests.InweboActionTestConfiguration.class,
    BaseInweboConfiguration.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.mfa.inwebo.client-certificate.certificate.location=classpath:clientcert.p12",
        "cas.authn.mfa.inwebo.client-certificate.passphrase=password",
        "cas.authn.mfa.inwebo.service-id=7046"
    })
@ExtendWith(CasTestExtension.class)
public abstract class BaseInweboActionTests {
    protected static final String LOGIN = "jerome@casinthecloud.com";

    protected static final String SESSION_ID = "12454312154564321";

    private static final ThreadLocal<InweboService> INWEBO_SERVICE = new ThreadLocal<>();

    private static final String DEVICE_NAME = "my device";

    protected MockRequestContext requestContext;

    protected InweboService service;

    protected CasWebflowEventResolver resolver;

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    protected TenantExtractor tenantExtractor;

    @Autowired
    private ApplicationContext applicationContext;
    
    protected static InweboDeviceNameResponse deviceResponse(final InweboResult result) {
        val response = new InweboDeviceNameResponse();
        response.setResult(result);
        if (result == InweboResult.OK) {
            response.setDeviceName(DEVICE_NAME);
        }
        return response;
    }

    @BeforeEach
    void setUp() throws Exception {
        this.service = mock(InweboService.class);
        INWEBO_SERVICE.set(this.service);
        this.requestContext = MockRequestContext.create(applicationContext);
        setAuthenticationInContext(LOGIN);
    }

    @AfterEach
    void tearDown() {
        INWEBO_SERVICE.remove();
    }

    protected void setAuthenticationInContext(final String id) {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(id);
        WebUtils.putAuthentication(authentication, requestContext);
        WebUtils.putAuthenticationResult(new DefaultAuthenticationResult(authentication, null), requestContext);
        val resultBuilder = new DefaultAuthenticationResultBuilder(new DefaultPrincipalElectionStrategy());
        resultBuilder.collect(authentication);
        WebUtils.putAuthenticationResultBuilder(resultBuilder, requestContext);
    }

    protected void assertMfa() throws Throwable {
        val builder = WebUtils.getAuthenticationResultBuilder(requestContext);
        val attributes = builder.build().getAuthentication().getAttributes();
        assertNotNull(attributes.get("inweboAuthenticationDevice"));
    }

    protected void assertNoMfa() throws Throwable {
        val builder = WebUtils.getAuthenticationResultBuilder(requestContext);
        val attributes = builder.build().getAuthentication().getAttributes();
        assertNull(attributes.get("inweboAuthenticationDevice"));
    }

    @TestConfiguration(value = "InweboActionTestConfiguration", proxyBeanMethods = false)
    public static class InweboActionTestConfiguration {
        /**
         * The Inwebo actions are singletons that capture this bean when the shared context is built,
         * so a single mock would be stubbed and reset by every test at once. This one forwards each
         * call to the mock the running test installed for its own thread, which lets the four action
         * test classes and their methods stub independently and run concurrently. Reflection wraps a
         * stubbed failure in an {@link InvocationTargetException}, so the cause is rethrown to keep
         * {@code thenThrow} stubbing behaving as the action under test expects.
         *
         * @return the inwebo service
         */
        @Bean
        public InweboService inweboService() {
            return mock(InweboService.class, withSettings().defaultAnswer(invocation -> {
                val delegate = INWEBO_SERVICE.get();
                if (delegate == null) {
                    return Answers.RETURNS_DEFAULTS.answer(invocation);
                }
                try {
                    return invocation.getMethod().invoke(delegate, invocation.getArguments());
                } catch (final InvocationTargetException e) {
                    throw e.getCause();
                }
            }));
        }
    }
}
