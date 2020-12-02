package org.apereo.cas.support.inwebo.web.flow.actions;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationResult;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionManager;
import org.apereo.cas.authentication.PolicyBasedAuthenticationManager;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.model.support.mfa.InweboMultifactorProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.inwebo.authentication.AuthenticationDeviceMetadataPopulator;
import org.apereo.cas.support.inwebo.authentication.InweboAuthenticationHandler;
import org.apereo.cas.support.inwebo.service.InweboService;
import org.apereo.cas.support.inwebo.service.response.DeviceNameResponse;
import org.apereo.cas.support.inwebo.service.response.Result;
import org.apereo.cas.support.inwebo.web.flow.InweboMultifactorAuthenticationWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.springframework.webflow.context.ExternalContextHolder.setExternalContext;
import static org.springframework.webflow.execution.RequestContextHolder.setRequestContext;

/**
 * This is the base class for action tests.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
public abstract class BaseActionTests {

    protected static final String LOGIN = "jerome@casinthecloud.com";
    protected static final String SESSION_ID = "12454312154564321";

    private static final String DEVICE_NAME = "my device";

    protected MockRequestContext requestContext;

    protected MockHttpServletRequest request;

    protected InweboService service;

    protected CasWebflowEventResolver resolver;

    @BeforeEach
    public void setUp() {
        requestContext = new MockRequestContext();
        request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        requestContext.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        setRequestContext(requestContext);
        setExternalContext(requestContext.getExternalContext());

        service = mock(InweboService.class);

        val authenticationEventExecutionPlan = new DefaultAuthenticationEventExecutionPlan();
        authenticationEventExecutionPlan.registerAuthenticationHandler(new InweboAuthenticationHandler(mock(ServicesManager.class),
                PrincipalFactoryUtils.newPrincipalFactory(), new InweboMultifactorProperties()));
        authenticationEventExecutionPlan.registerAuthenticationMetadataPopulator(new AuthenticationDeviceMetadataPopulator());
        val authenticationManager = new PolicyBasedAuthenticationManager(authenticationEventExecutionPlan, true, mock(ConfigurableApplicationContext.class));
        val authenticationTransactionManager = new DefaultAuthenticationTransactionManager(mock(ApplicationEventPublisher.class), authenticationManager);
        val authenticationSystemSupport = new DefaultAuthenticationSystemSupport(authenticationTransactionManager, new DefaultPrincipalElectionStrategy());
        val context = CasWebflowEventResolutionConfigurationContext.builder()
                .authenticationSystemSupport(authenticationSystemSupport).build();
        resolver = new InweboMultifactorAuthenticationWebflowEventResolver(context);

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

    protected DeviceNameResponse deviceResponse(final Result result) {
        val response = new DeviceNameResponse();
        response.setResult(result);
        if (result == Result.OK) {
            response.setDeviceName(DEVICE_NAME);
        }
        return response;
    }

    protected void assertMfa() {
        val builder = WebUtils.getAuthenticationResultBuilder(requestContext);
        val attributes = builder.build(new DefaultPrincipalElectionStrategy()).getAuthentication().getAttributes();
        assertNotNull(attributes.get("inweboAuthenticationDevice"));
    }

    protected void assertNoMfa() {
        val builder = WebUtils.getAuthenticationResultBuilder(requestContext);
        val attributes = builder.build(new DefaultPrincipalElectionStrategy()).getAuthentication().getAttributes();
        assertNull(attributes.get("inweboAuthenticationDevice"));
    }
}
