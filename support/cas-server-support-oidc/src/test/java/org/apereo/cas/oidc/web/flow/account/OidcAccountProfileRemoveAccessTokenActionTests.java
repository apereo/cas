package org.apereo.cas.oidc.web.flow.account;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcAccountProfileRemoveAccessTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OIDCWeb")
class OidcAccountProfileRemoveAccessTokenActionTests extends AbstractOidcTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_REMOVE_OIDC_ACCESS_TOKEN)
    private Action oidcAccountProfileRemoveAccessTokensAction;

    @Test
    void verifyOperation() throws Throwable {
        val accessToken = getAccessToken(UUID.randomUUID().toString());
        ticketRegistry.addTicket(accessToken);
        val context = MockRequestContext.create(applicationContext);
        context.setParameter("id", accessToken.getId());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS,
            oidcAccountProfileRemoveAccessTokensAction.execute(context).getId());
        assertNull(ticketRegistry.getTicket(accessToken.getId()));
    }
}
