package org.apereo.cas.aup;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.config.CasAcceptableUsagePolicyWebflowConfiguration;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCookieConfiguration;
import org.apereo.cas.config.CasCoreAuditConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreLogoutConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreTicketsSerializationConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasCoreWebflowConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.CasWebflowContextConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Flow;
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

    @Autowired
    private ConfigurableApplicationContext applicationContext;

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
        final Authentication authentication, final boolean expectPolicyFound) throws Exception {
        val context = MockRequestContext.create();
        val flowDefinition = mock(Flow.class);
        when(flowDefinition.getApplicationContext()).thenReturn(applicationContext);
        context.setActiveFlow(flowDefinition);

        WebUtils.putRegisteredService(context, service);
        WebUtils.putAuthentication(authentication, context);
        assertEquals(expectPolicyFound, getAcceptableUsagePolicyRepository().fetchPolicy(context).isPresent());
    }

    protected void verifyRepositoryAction(final String actualPrincipalId,
        final Map<String, List<Object>> profileAttributes) throws Throwable {
        val credential = getCredential(actualPrincipalId);
        val context = getRequestContext(actualPrincipalId, profileAttributes, credential);

        assertTrue(getAcceptableUsagePolicyRepository().verify(context).isDenied());
        assertTrue(getAcceptableUsagePolicyRepository().submit(context));
        if (hasLiveUpdates()) {
            assertTrue(getAcceptableUsagePolicyRepository().verify(context).isAccepted());
        }
    }
    
    protected static UsernamePasswordCredential getCredential(final String actualPrincipalId) {
        return CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(actualPrincipalId);
    }

    protected MockRequestContext getRequestContext(final String actualPrincipalId,
                                                   final Map<String, List<Object>> profileAttributes,
                                                   final Credential credential) throws Throwable {
        val context = MockRequestContext.create();
        val tgt = new MockTicketGrantingTicket(actualPrincipalId, credential, profileAttributes);
        ticketRegistry.addTicket(tgt);
        val principal = CoreAuthenticationTestUtils.getPrincipal(credential.getId(), profileAttributes);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(principal), context);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        return context;
    }

    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasCoreTicketsConfiguration.class,
        CasCoreTicketIdGeneratorsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreTicketsSerializationConfiguration.class,
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
        CasCoreUtilConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasWebflowContextConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasPersonDirectoryTestConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class
    })
    public static class SharedTestConfiguration {
    }
}
