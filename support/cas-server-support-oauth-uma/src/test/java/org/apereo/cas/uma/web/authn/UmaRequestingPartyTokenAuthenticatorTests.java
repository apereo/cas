package org.apereo.cas.uma.web.authn;

import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UmaRequestingPartyTokenAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("UMA")
public class UmaRequestingPartyTokenAuthenticatorTests extends BaseUmaEndpointControllerTests {

    @Test
    public void verifyOperation() throws Exception {
        val input = new UmaRequestingPartyTokenAuthenticator(ticketRegistry, accessTokenJwtBuilder);
        val token = getAccessToken();
        val credentials = new TokenCredentials(token.getId());
        ticketRegistry.addTicket(token);
        assertThrows(CredentialsException.class, () -> {
            val webContext = new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse());
            input.validate(new CallContext(webContext, JEESessionStore.INSTANCE), credentials);
        });
    }

}
