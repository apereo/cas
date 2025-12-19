package org.apereo.cas.web.flow.account;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.flow.AbstractWebflowActionsTests;
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
 * This is {@link AccountProfileRemoveSingleSignOnSessionActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("WebflowAccountActions")
@TestPropertySource(properties = "CasFeatureModule.AccountManagement.enabled=true")
@ImportAutoConfiguration({
    CasCoreWebflowAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class
})
class AccountProfileRemoveSingleSignOnSessionActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_REMOVE_SINGLE_SIGNON_SESSION)
    private Action removeSessionAction;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val tgtId = RandomUtils.randomAlphabetic(8);
        val tgt = new TicketGrantingTicketImpl(tgtId,
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        getTicketRegistry().addTicket(tgt);
        context.setParameter("id", tgtId);
        assertNotNull(getTicketRegistry().getTicket(tgtId));
        val result = removeSessionAction.execute(context);
        assertNull(getTicketRegistry().getTicket(tgtId));
        assertEquals(CasWebflowConstants.TRANSITION_ID_VALIDATE, result.getId());
    }
}
