package org.apereo.cas.aup;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasAcceptableUsagePolicyWebflowConfiguration;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
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
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link BaseAcceptableUsagePolicyRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasAcceptableUsagePolicyWebflowConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreWebConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class
})
public abstract class BaseAcceptableUsagePolicyRepositoryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Autowired
    @Qualifier("ticketRegistry")
    protected TicketRegistry ticketRegistry;

    public abstract AcceptableUsagePolicyRepository getAcceptableUsagePolicyRepository();
    /**
     * Repository can update the state of the AUP acceptance without reloading the principal. Mostly for testing purposes.
     *
     * @return live updates are possible.
     */
    public boolean hasLiveUpdates() {
        return false;
    }

    @Test
    public void verifyRepositoryAction() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser");
        val tgt = new MockTicketGrantingTicket("casuser", c, CollectionUtils.wrap("accepted", "false"));
        ticketRegistry.addTicket(tgt);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        assertFalse(getAcceptableUsagePolicyRepository().verify(context, c).getLeft());
        assertTrue(getAcceptableUsagePolicyRepository().submit(context, c));
        if (hasLiveUpdates()) {
            assertTrue(getAcceptableUsagePolicyRepository().verify(context, c).getLeft());
        }
    }
}
