package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccountProfilePasswordChangeRequestActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("WebflowAccountActions")
@TestPropertySource(properties = "CasFeatureModule.AccountManagement.enabled=true")
@ImportAutoConfiguration(CasCoreWebflowAutoConfiguration.class)
class AccountProfilePasswordChangeRequestActionTests extends BasePasswordManagementActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_PASSWORD_CHANGE_REQUEST)
    private Action accountProfilePasswordChangeRequestAction;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val tgt = new MockTicketGrantingTicket("casuser");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putTicketGrantingTicket(context, tgt);
        ticketRegistry.addTicket(tgt);

        val result = accountProfilePasswordChangeRequestAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        assertNotNull(WebUtils.getServiceRedirectUrl(context));
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(tgt.getId(), TicketGrantingTicket.class));
    }
}
