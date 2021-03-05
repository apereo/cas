package org.apereo.cas.authentication.handler;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationTransactionFactory;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ByCredentialTypeAuthenticationHandlerResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Authentication")
public class ByCredentialTypeAuthenticationHandlerResolverTests {

    @Test
    public void verifySupports() {
        val resolver = new ByCredentialTypeAuthenticationHandlerResolver(UsernamePasswordCredential.class);
        assertTrue(resolver.supports(CollectionUtils.wrapSet(new SimpleTestUsernamePasswordAuthenticationHandler()),
            new DefaultAuthenticationTransactionFactory().newTransaction(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword())));
    }

    @Test
    public void verifyResolves() {
        val resolver = new ByCredentialTypeAuthenticationHandlerResolver(UsernamePasswordCredential.class);
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        c.setSource("TestHandler");
        val handler = new SimpleTestUsernamePasswordAuthenticationHandler("TESTHANDLER");
        val results = resolver.resolve(CollectionUtils.wrapSet(handler), new DefaultAuthenticationTransactionFactory().newTransaction(c));
        assertFalse(results.isEmpty());
    }
}
