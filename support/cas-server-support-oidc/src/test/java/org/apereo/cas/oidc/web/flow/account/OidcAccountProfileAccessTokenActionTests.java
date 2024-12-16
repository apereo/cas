package org.apereo.cas.oidc.web.flow.account;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAccountProfileAccessTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OIDCWeb")
class OidcAccountProfileAccessTokenActionTests extends AbstractOidcTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_ACCESS_TOKENS)
    private Action oidcAccountProfileAccessTokensAction;

    @Test
    void verifyOperation() throws Throwable {
        val accessToken = getAccessToken(UUID.randomUUID().toString());
        ticketRegistry.addTicket(accessToken);
        val tgt = new MockTicketGrantingTicket(accessToken.getAuthentication());
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        assertNull(oidcAccountProfileAccessTokensAction.execute(context));
        val oidcAccessTokens = context.getFlowScope().get("oidcAccessTokens", List.class);
        assertNotNull(oidcAccessTokens);
        assertEquals(1, oidcAccessTokens.size());
    }
}
