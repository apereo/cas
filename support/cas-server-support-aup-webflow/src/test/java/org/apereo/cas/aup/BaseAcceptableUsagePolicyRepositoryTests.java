package org.apereo.cas.aup;

import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.config.CasAcceptableUsagePolicyWebflowConfiguration;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.message.MessageContext;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockParameterMap;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link BaseAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@SpringBootTest(classes = BaseAcceptableUsagePolicyRepositoryTests.SharedTestConfiguration.class)
public abstract class BaseAcceptableUsagePolicyRepositoryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    protected TicketRegistry ticketRegistry;

    @Autowired
    protected CasConfigurationProperties casProperties;

    public abstract AcceptableUsagePolicyRepository getAcceptableUsagePolicyRepository();

    /**
     * Repository can update the state of the AUP acceptance without reloading the principal. Mostly for testing purposes.
     *
     * @return live updates are possible.
     */
    public boolean hasLiveUpdates() {
        return false;
    }

    protected void verifyFetchingPolicy(final RegisteredService service,
        final Authentication authentication,
        final boolean expectPolicyFound) {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val context = mock(RequestContext.class);
        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        when(context.getRequestParameters()).thenReturn(new MockParameterMap());
        when(context.getFlowScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getConversationScope()).thenReturn(new LocalAttributeMap<>());
        val flowDefn = mock(FlowDefinition.class);
        when(flowDefn.getApplicationContext()).thenReturn(applicationContext);
        when(context.getActiveFlow()).thenReturn(flowDefn);

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        when(context.getExternalContext()).thenReturn(new ServletExternalContext(new MockServletContext(), request, response));

        WebUtils.putRegisteredService(context, service);
        WebUtils.putAuthentication(authentication, context);
        assertEquals(expectPolicyFound, getAcceptableUsagePolicyRepository().fetchPolicy(context).isPresent());
    }

    protected void verifyRepositoryAction(final String actualPrincipalId,
        final Map<String, List<Object>> profileAttributes) {
        val c = getCredential(actualPrincipalId);
        val context = getRequestContext(actualPrincipalId, profileAttributes, c);

        assertFalse(getAcceptableUsagePolicyRepository().verify(context).isAccepted());
        assertTrue(getAcceptableUsagePolicyRepository().submit(context));
        if (hasLiveUpdates()) {
            assertTrue(getAcceptableUsagePolicyRepository().verify(context).isAccepted());
        }
    }
    
    protected UsernamePasswordCredential getCredential(final String actualPrincipalId) {
        return CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(actualPrincipalId);
    }

    protected MockRequestContext getRequestContext(final String actualPrincipalId,
        final Map<String, List<Object>> profileAttributes,
        final Credential c) {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        val tgt = new MockTicketGrantingTicket(actualPrincipalId, c, profileAttributes);
        ticketRegistry.addTicket(tgt);
        val principal = CoreAuthenticationTestUtils.getPrincipal(c.getId(), profileAttributes);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(principal), context);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        return context;
    }

    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasCoreTicketsConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreAuditConfiguration.class,
        CasRegisteredServicesTestConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreMultifactorAuthenticationConfiguration.class,
        CasMultifactorAuthenticationWebflowConfiguration.class,
        CasCoreAuthenticationConfiguration.class,
        CasAuthenticationEventExecutionPlanTestConfiguration.class,
        CasDefaultServiceTicketIdGeneratorsConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasAcceptableUsagePolicyWebflowConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasWebflowContextConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
