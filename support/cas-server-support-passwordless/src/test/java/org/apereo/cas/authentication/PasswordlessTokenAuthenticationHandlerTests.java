package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.OneTimePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.impl.token.InMemoryPasswordlessTokenRepository;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PasswordlessTokenAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("AuthenticationHandler")
public class PasswordlessTokenAuthenticationHandlerTests {
    @Test
    public void verifyAction() throws Exception {
        val repository = new InMemoryPasswordlessTokenRepository(60);
        repository.saveToken("casuser", "123456");
        val handler = new PasswordlessTokenAuthenticationHandler(null,
            mock(ServicesManager.class),
            PrincipalFactoryUtils.newPrincipalFactory(), 0, repository);
        val credential = new OneTimePasswordCredential("casuser", "123456");
        credential.setCredentialMetadata(new BasicCredentialMetadata(credential));
        assertNotNull(handler.authenticate(credential, mock(Service.class)));

        assertThrows(FailedLoginException.class, () -> handler.authenticate(new OneTimePasswordCredential("1", "2"), mock(Service.class)));

        assertTrue(handler.supports(credential));
        assertTrue(handler.supports(credential.getCredentialMetadata().getCredentialClass()));
        assertFalse(handler.supports(new UsernamePasswordCredential()));
    }
}
