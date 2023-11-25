package org.apereo.cas.support.inwebo.web.flow.actions;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationManager;
import org.apereo.cas.authentication.DefaultAuthenticationResult;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.model.support.mfa.InweboMultifactorAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.inwebo.authentication.InweboAuthenticationDeviceMetadataPopulator;
import org.apereo.cas.support.inwebo.authentication.InweboAuthenticationHandler;
import org.apereo.cas.support.inwebo.service.InweboService;
import org.apereo.cas.support.inwebo.service.response.InweboDeviceNameResponse;
import org.apereo.cas.support.inwebo.service.response.InweboResult;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.DirectObjectProvider;
import org.apereo.cas.web.flow.authentication.FinalMultifactorAuthenticationTransactionWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.support.StaticApplicationContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is the base class for action tests.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
public abstract class BaseInweboActionTests {

    protected static final String LOGIN = "jerome@casinthecloud.com";

    protected static final String SESSION_ID = "12454312154564321";

    private static final String DEVICE_NAME = "my device";

    protected MockRequestContext requestContext;

    protected InweboService service;

    protected CasWebflowEventResolver resolver;

    protected static InweboDeviceNameResponse deviceResponse(final InweboResult result) {
        val response = new InweboDeviceNameResponse();
        response.setResult(result);
        if (result == InweboResult.OK) {
            response.setDeviceName(DEVICE_NAME);
        }
        return response;
    }

    @BeforeEach
    public void setUp() throws Exception {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        var authenticationSystemSupport = ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            CoreAuthenticationTestUtils.getAuthenticationSystemSupport(), AuthenticationSystemSupport.BEAN_NAME);

        this.requestContext = MockRequestContext.create(applicationContext);
        service = mock(InweboService.class);

        val authenticationEventExecutionPlan = new DefaultAuthenticationEventExecutionPlan();
        authenticationEventExecutionPlan.registerAuthenticationHandler(new InweboAuthenticationHandler(mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), new InweboMultifactorAuthenticationProperties(), service,
            new DirectObjectProvider<>(mock(MultifactorAuthenticationProvider.class))));
        authenticationEventExecutionPlan.registerAuthenticationMetadataPopulator(new InweboAuthenticationDeviceMetadataPopulator());

        val authenticationManager = new DefaultAuthenticationManager(authenticationEventExecutionPlan,
            new DirectObjectProvider<>(authenticationSystemSupport), true, applicationContext);
        authenticationSystemSupport = CoreAuthenticationTestUtils.getAuthenticationSystemSupport(authenticationManager, mock(ServicesManager.class));
        val configurationContext = CasWebflowEventResolutionConfigurationContext.builder().authenticationSystemSupport(authenticationSystemSupport).build();
        resolver = new FinalMultifactorAuthenticationTransactionWebflowEventResolver(configurationContext);
        setAuthenticationInContext(LOGIN);
    }

    protected void setAuthenticationInContext(final String id) {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(id);
        WebUtils.putAuthentication(authentication, requestContext);
        WebUtils.putAuthenticationResult(new DefaultAuthenticationResult(authentication, null), requestContext);
        val resultBuilder = new DefaultAuthenticationResultBuilder();
        resultBuilder.collect(authentication);
        WebUtils.putAuthenticationResultBuilder(resultBuilder, requestContext);
    }

    protected void assertMfa() throws Throwable {
        val builder = WebUtils.getAuthenticationResultBuilder(requestContext);
        val attributes = builder.build(new DefaultPrincipalElectionStrategy()).getAuthentication().getAttributes();
        assertNotNull(attributes.get("inweboAuthenticationDevice"));
    }

    protected void assertNoMfa() throws Throwable {
        val builder = WebUtils.getAuthenticationResultBuilder(requestContext);
        val attributes = builder.build(new DefaultPrincipalElectionStrategy()).getAuthentication().getAttributes();
        assertNull(attributes.get("inweboAuthenticationDevice"));
    }
}
