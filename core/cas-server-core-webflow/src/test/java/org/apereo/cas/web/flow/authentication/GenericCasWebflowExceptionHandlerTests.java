package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.util.MockRequestContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;

import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GenericCasWebflowExceptionHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("AuthenticationHandler")
class GenericCasWebflowExceptionHandlerTests {
    @Test
    void verifyOperation() throws Throwable {
        val errors = new LinkedHashSet<Class<? extends Throwable>>();
        errors.add(AccountLockedException.class);
        errors.add(CredentialExpiredException.class);
        errors.add(AccountExpiredException.class);
        val catalog = new DefaultCasWebflowExceptionCatalog();
        catalog.registerExceptions(errors);

        val context = MockRequestContext.create();

        val handler = new GenericCasWebflowExceptionHandler(catalog);
        assertTrue(handler.supports(new AccountExpiredException(), context));

        val event = handler.handle(new CredentialExpiredException(), context);
        assertNotNull(event);
        assertEquals(CasWebflowExceptionCatalog.UNKNOWN, event.getId());
    }
}
