package org.apereo.cas.support.wsfederation.authentication.principal;

import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.DefaultAuthenticationTransaction;
import org.apereo.cas.support.wsfederation.AbstractWsFederationTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WsFederationCredentialsToPrincipalResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class WsFederationCredentialsToPrincipalResolverTests extends AbstractWsFederationTests {
    @Autowired
    @Qualifier("casAuthenticationManager")
    private AuthenticationManager authenticationManager;

    @Test
    public void verifyAuth() throws Exception {
        val creds = getCredential();
        val auth = authenticationManager.authenticate(DefaultAuthenticationTransaction.of(creds));
        assertNotNull(auth);
    }
}
