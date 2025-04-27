package org.apereo.cas.authentication;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.credential.OneTimePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;
import org.apereo.cas.impl.token.InMemoryPasswordlessTokenRepository;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import javax.security.auth.login.FailedLoginException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PasswordlessTokenAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("AuthenticationHandler")
class PasswordlessTokenAuthenticationHandlerTests {

    @Nested
    @TestPropertySource(properties = "cas.authn.passwordless.core.enabled=false")
    class DisabledTests extends BasePasswordlessUserAccountStoreTests {

        @Autowired
        @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
        private AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

        @Test
        void verifyDisable() {
            val authenticationHandlers = authenticationEventExecutionPlan.resolveAuthenticationHandlersBy(PasswordlessTokenAuthenticationHandler.class::isInstance);
            assertTrue(authenticationHandlers.isEmpty());
        }
    }

    @Nested
    class DefaultTests {
        @Test
        void verifyAction() throws Throwable {
            val repository = new InMemoryPasswordlessTokenRepository(60, CipherExecutor.noOpOfSerializableToString());

            val uid = UUID.randomUUID().toString();
            val passwordlessUserAccount = PasswordlessUserAccount.builder().username(uid).build();
            val passwordlessRequest = PasswordlessAuthenticationRequest.builder().username(uid).build();
            var token = repository.createToken(passwordlessUserAccount, passwordlessRequest);
            token = repository.saveToken(passwordlessUserAccount, passwordlessRequest, token);
            val handler = new PasswordlessTokenAuthenticationHandler(null,
                mock(ServicesManager.class),
                PrincipalFactoryUtils.newPrincipalFactory(), 0, repository);
            val credential = new OneTimePasswordCredential(uid, token.getToken());
            credential.setCredentialMetadata(new BasicCredentialMetadata(credential));
            assertNotNull(handler.authenticate(credential, mock(Service.class)));

            assertThrows(FailedLoginException.class,
                () -> handler.authenticate(new OneTimePasswordCredential("1", "2"), mock(Service.class)));
            assertThrows(FailedLoginException.class,
                () -> handler.authenticate(new OneTimePasswordCredential(credential.getId(), "123456"), mock(Service.class)));

            assertTrue(handler.supports(credential));
            assertTrue(handler.supports(credential.getCredentialMetadata().getCredentialClass()));
            assertFalse(handler.supports(new UsernamePasswordCredential()));
        }
    }
}
