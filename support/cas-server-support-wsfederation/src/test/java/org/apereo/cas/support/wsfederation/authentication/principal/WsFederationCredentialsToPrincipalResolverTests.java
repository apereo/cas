package org.apereo.cas.support.wsfederation.authentication.principal;

import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionFactory;
import org.apereo.cas.support.wsfederation.AbstractWsFederationTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WsFederationCredentialsToPrincipalResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WSFederation")
public class WsFederationCredentialsToPrincipalResolverTests extends AbstractWsFederationTests {
    @Autowired
    @Qualifier("casAuthenticationManager")
    private AuthenticationManager authenticationManager;

    @Test
    public void verifyAuth() {
        val creds = getCredential();
        val auth = authenticationManager.authenticate(new DefaultAuthenticationTransactionFactory().newTransaction(creds));
        assertNotNull(auth);
    }

    @Test
    public void verifyMultipleAttributes() {
        val attributes = new HashMap<>(CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap());
        attributes.put("upn", List.of("cas1", "cas2"));
        val creds = getCredential(attributes);
        val auth = authenticationManager.authenticate(new DefaultAuthenticationTransactionFactory().newTransaction(creds));
        assertNotNull(auth);
    }

}
