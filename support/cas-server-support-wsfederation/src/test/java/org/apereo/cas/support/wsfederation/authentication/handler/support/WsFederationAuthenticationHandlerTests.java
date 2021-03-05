package org.apereo.cas.support.wsfederation.authentication.handler.support;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.support.wsfederation.AbstractWsFederationTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.FailedLoginException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WsFederationAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WSFederation")
public class WsFederationAuthenticationHandlerTests extends AbstractWsFederationTests {
    @Test
    public void verifyOperation() {
        val handler = new WsFederationAuthenticationHandler(UUID.randomUUID().toString(), servicesManager,
            PrincipalFactoryUtils.newPrincipalFactory(), 0);
        assertTrue(handler.supports(getCredential()));
        assertTrue(handler.supports(getCredential().getClass()));
        assertThrows(FailedLoginException.class, () -> handler.authenticate(null));
    }

}
