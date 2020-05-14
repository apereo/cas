package org.apereo.cas.authentication.exceptions;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

import javax.security.auth.login.FailedLoginException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UnresolvedPrincipalExceptionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class UnresolvedPrincipalExceptionTests {
    @Test
    public void verifyOperation() {
        assertDoesNotThrow((ThrowingSupplier<UnresolvedPrincipalException>) UnresolvedPrincipalException::new);
        assertDoesNotThrow(() -> new UnresolvedPrincipalException(CoreAuthenticationTestUtils.getAuthentication()));
        assertDoesNotThrow(() -> new UnresolvedPrincipalException(new FailedLoginException()));
        assertDoesNotThrow(() -> new UnresolvedPrincipalException(Map.of("Failure", new FailedLoginException())));
        assertDoesNotThrow(() -> new UnresolvedPrincipalException(CoreAuthenticationTestUtils.getAuthentication(), new FailedLoginException()));
    }
}
