package org.apereo.cas.support.inwebo.web.flow.actions;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationManager;
import org.apereo.cas.authentication.DefaultAuthenticationResult;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilderFactory;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionFactory;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionManager;
import org.apereo.cas.authentication.principal.DefaultPrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.model.support.mfa.InweboMultifactorAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.inwebo.authentication.InweboAuthenticationDeviceMetadataPopulator;
import org.apereo.cas.support.inwebo.authentication.InweboAuthenticationHandler;
import org.apereo.cas.support.inwebo.service.InweboService;
import org.apereo.cas.support.inwebo.service.response.InweboDeviceNameResponse;
import org.apereo.cas.support.inwebo.service.response.InweboResult;
import org.apereo.cas.support.inwebo.web.flow.InweboMultifactorAuthenticationWebflowEventResolver;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.binding.message.DefaultMessageContext;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.text.MessageFormat;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.webflow.context.ExternalContextHolder.*;
import static org.springframework.webflow.execution.RequestContextHolder.*;

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

    protected static InweboDeviceNameResponse deviceResponse(final InweboResult result) {
        val response = new InweboDeviceNameResponse();
        response.setResult(result);
        if (result == InweboResult.OK) {
            response.setDeviceName(DEVICE_NAME);
        }
        return response;
    }

    @BeforeEach
    public void setUp() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            CoreAuthenticationTestUtils.getAuthenticationSystemSupport(), AuthenticationSystemSupport.BEAN_NAME);

        requestContext = new MockRequestContext();
        request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        requestContext.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        setRequestContext(requestContext);
        setExternalContext(requestContext.getExternalContext());
        ((DefaultMessageContext) requestContext.getMessageContext()).setMessageSource(new AbstractMessageSource() {
            @Override
            protected MessageFormat resolveCode(final String code, final Locale locale) {
                return new MessageFormat(StringUtils.EMPTY);
            }
        });

        service = mock(InweboService.class);

        val authenticationEventExecutionPlan = new DefaultAuthenticationEventExecutionPlan();
        authenticationEventExecutionPlan.registerAuthenticationHandler(new InweboAuthenticationHandler(mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), new InweboMultifactorAuthenticationProperties(), service));
        authenticationEventExecutionPlan.registerAuthenticationMetadataPopulator(new InweboAuthenticationDeviceMetadataPopulator());
        val authenticationManager = new DefaultAuthenticationManager(authenticationEventExecutionPlan,
            true, applicationContext);
        val authenticationTransactionManager = new DefaultAuthenticationTransactionManager(applicationContext, authenticationManager);
        val authenticationSystemSupport = new DefaultAuthenticationSystemSupport(authenticationTransactionManager, new DefaultPrincipalElectionStrategy(),
            new DefaultAuthenticationResultBuilderFactory(), new DefaultAuthenticationTransactionFactory());
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
