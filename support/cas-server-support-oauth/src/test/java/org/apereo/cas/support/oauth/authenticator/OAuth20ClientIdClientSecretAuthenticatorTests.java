package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20ClientIdClientSecretAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class OAuth20ClientIdClientSecretAuthenticatorTests extends BaseOAuth20AuthenticatorTests {
    protected OAuth20ClientIdClientSecretAuthenticator authenticator;

    @Override
    public void initialize() {
        super.initialize();
        authenticator = new OAuth20ClientIdClientSecretAuthenticator(servicesManager, serviceFactory, new RegisteredServiceAccessStrategyAuditableEnforcer());
    }

    @Test
    public void verifyAuthentication() {
        val credentials = new UsernamePasswordCredentials("client", "secret");
        val request = new MockHttpServletRequest();
        val ctx = new J2EContext(request, new MockHttpServletResponse());
        authenticator.validate(credentials, ctx);
        assertNotNull(credentials.getUserProfile());
        assertEquals("client", credentials.getUserProfile().getId());
    }
}
