package org.apereo.cas.authentication.handler;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ByCredentialSourceAuthenticationHandlerResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("AuthenticationHandler")
class ByCredentialSourceAuthenticationHandlerResolverTests {

    @Test
    void verifySupports() {
        val resolver = new ByCredentialSourceAuthenticationHandlerResolver();
        assertTrue(resolver.supports(CollectionUtils.wrapSet(new SimpleTestUsernamePasswordAuthenticationHandler()),
            CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword())));
    }

    @Test
    void verifyResolves() {
        val resolver = new ByCredentialSourceAuthenticationHandlerResolver();
        val c = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        c.setSource("TestHandler");
        val handler = new SimpleTestUsernamePasswordAuthenticationHandler("TESTHANDLER");
        val results = resolver.resolve(CollectionUtils.wrapSet(handler), CoreAuthenticationTestUtils.getAuthenticationTransactionFactory().newTransaction(c));
        assertFalse(results.isEmpty());
    }
}
