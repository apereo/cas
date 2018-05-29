package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.impl.token.InMemoryPasswordlessTokenRepository;
import org.apereo.cas.services.ServicesManager;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PasswordlessTokenAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class PasswordlessTokenAuthenticationHandlerTests {
    @Test
    public void verifyAction() throws Exception {
        final var repository = new InMemoryPasswordlessTokenRepository(60);
        repository.saveToken("casuser", "123456");
        final AuthenticationHandler h = new PasswordlessTokenAuthenticationHandler(null,
            mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), 0, repository);
        final var c = new OneTimePasswordCredential("casuser", "123456");
        assertNotNull(h.authenticate(c));
    }
}
