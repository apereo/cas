package org.apereo.cas.web.flow.actions;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DefaultSingleSignOnParticipationStrategy;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RenewAuthenticationRequestCheckActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowAuthenticationActions")
class RenewAuthenticationRequestCheckActionTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;
    
    @Test
    void verifyProceed() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val strategy = new DefaultSingleSignOnParticipationStrategy(servicesManager, casProperties.getSso(),
            mock(TicketRegistrySupport.class), mock(AuthenticationServiceSelectionPlan.class));
        val action = new RenewAuthenticationRequestCheckAction(strategy);
        assertEquals(CasWebflowConstants.TRANSITION_ID_PROCEED, action.execute(context).getId());
    }

    @Test
    void verifyRenew() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
        val strategy = new DefaultSingleSignOnParticipationStrategy(servicesManager, casProperties.getSso(),
            mock(TicketRegistrySupport.class), mock(AuthenticationServiceSelectionPlan.class));
        val action = new RenewAuthenticationRequestCheckAction(strategy);
        assertEquals(CasWebflowConstants.TRANSITION_ID_RENEW, action.execute(context).getId());
    }


}
