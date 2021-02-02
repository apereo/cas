package org.apereo.cas.uma.web.authn;

import org.apereo.cas.uma.web.controllers.BaseUmaEndpointControllerTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.exception.CredentialsException;
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
    public void verifyOperation() {
        val input = new UmaRequestingPartyTokenAuthenticator(centralAuthenticationService, accessTokenJwtBuilder);
        val token = getAccessToken();
        val credentials = new TokenCredentials(token.getId());
        centralAuthenticationService.addTicket(token);
        assertThrows(CredentialsException.class, new Executable() {
            @Override
            public void execute() {
                val webContext = new JEEContext(new MockHttpServletRequest(), new MockHttpServletResponse());
                input.validate(credentials, webContext, JEESessionStore.INSTANCE);
            }
        });
    }

}
