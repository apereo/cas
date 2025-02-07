package org.apereo.cas.support.inwebo.web.flow.actions;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
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
@Execution(ExecutionMode.SAME_THREAD)
public abstract class BaseInweboActionTests {

    protected static final String LOGIN = "jerome@casinthecloud.com";

    protected static final String SESSION_ID = "12454312154564321";

    private static final String DEVICE_NAME = "my device";

    protected MockRequestContext requestContext;

    @Autowired
    @Qualifier("inweboService")
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
        this.requestContext = MockRequestContext.create(applicationContext);
        reset(this.service);
        setAuthenticationInContext(LOGIN);
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
        @Bean
        public InweboService inweboService() {
            return mock(InweboService.class);
        }
    }
}
