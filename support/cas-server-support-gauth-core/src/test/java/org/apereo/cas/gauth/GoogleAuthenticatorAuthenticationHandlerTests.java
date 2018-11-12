package org.apereo.cas.gauth;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.OneTimeToken;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.gauth.credential.InMemoryGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.CachingOneTimeTokenRepository;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.ICredentialRepository;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GoogleAuthenticatorAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest
public class GoogleAuthenticatorAuthenticationHandlerTests {
    private IGoogleAuthenticator googleAuthenticator;
    private GoogleAuthenticatorAuthenticationHandler handler;
    private GoogleAuthenticatorKey googleAuthenticatorAccount;

    @BeforeEach
    public void initialize() {
        val servicesManager = mock(ServicesManager.class);
        val bldr = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        googleAuthenticator = new GoogleAuthenticator(bldr.build());
        googleAuthenticator.setCredentialRepository(new DummyCredentialRepository());
        handler = new GoogleAuthenticatorAuthenticationHandler("GAuth",
            servicesManager,
            PrincipalFactoryUtils.newPrincipalFactory(),
            googleAuthenticator,
            new CachingOneTimeTokenRepository(Caffeine.newBuilder().initialCapacity(10).build(s -> null)),
            new InMemoryGoogleAuthenticatorTokenCredentialRepository(CipherExecutor.noOpOfStringToString(), googleAuthenticator),
            null);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication("casuser"), context);
    }

    @Test
    public void verifySupports() {
        val credential = new GoogleAuthenticatorTokenCredential();
        assertTrue(handler.supports(credential));
        assertTrue(handler.supports(GoogleAuthenticatorTokenCredential.class));
    }

    @Test
    public void verifyAuthnAccountNotFound() throws Exception {
        val credential = getGoogleAuthenticatorTokenCredential();
        assertThrows(AccountNotFoundException.class, () -> {
            handler.authenticate(credential);
        });
    }

    @Test
    public void verifyAuthnFailsTokenNotFound() throws Exception {
        val credential = getGoogleAuthenticatorTokenCredential();
        handler.getTokenRepository().store(new OneTimeToken(Integer.valueOf(credential.getToken()), "casuser"));
        handler.getCredentialRepository().save("casuser", googleAuthenticatorAccount.getKey(),
            googleAuthenticatorAccount.getVerificationCode(), googleAuthenticatorAccount.getScratchCodes());
        assertThrows(AccountExpiredException.class, () -> {
            handler.authenticate(credential);
        });
    }

    @Test
    public void verifyAuthnTokenFound() throws Exception {
        val credential = getGoogleAuthenticatorTokenCredential();
        handler.getCredentialRepository().save("casuser", googleAuthenticatorAccount.getKey(),
            googleAuthenticatorAccount.getVerificationCode(), googleAuthenticatorAccount.getScratchCodes());
        val result = handler.authenticate(credential);
        assertNotNull(result);
        assertNotNull(handler.getTokenRepository().get("casuser", Integer.valueOf(credential.getToken())));
    }

    @Test
    public void verifyAuthnTokenScratchCode() throws Exception {
        val credential = getGoogleAuthenticatorTokenCredential();
        handler.getCredentialRepository().save("casuser", googleAuthenticatorAccount.getKey(),
            googleAuthenticatorAccount.getVerificationCode(), googleAuthenticatorAccount.getScratchCodes());
        credential.setToken(Integer.toString(googleAuthenticatorAccount.getScratchCodes().get(0)));
        val result = handler.authenticate(credential);
        assertNotNull(result);
        val otp = Integer.valueOf(credential.getToken());
        assertNotNull(handler.getTokenRepository().get("casuser", otp));
        assertFalse(handler.getCredentialRepository().get("casuser").getScratchCodes().contains(otp));
    }

    private GoogleAuthenticatorTokenCredential getGoogleAuthenticatorTokenCredential() {
        val credential = new GoogleAuthenticatorTokenCredential();
        googleAuthenticatorAccount = googleAuthenticator.createCredentials("casuser");
        val key = googleAuthenticator.getTotpPassword(googleAuthenticatorAccount.getKey());
        credential.setToken(Integer.toString(key));
        return credential;
    }

    private static class DummyCredentialRepository implements ICredentialRepository {
        private final Map<String, String> accounts = new LinkedHashMap<>();

        @Override
        public String getSecretKey(final String s) {
            return accounts.get(s);
        }

        @Override
        public void saveUserCredentials(final String s, final String s1, final int i, final List<Integer> list) {
            accounts.put(s, s1);
        }
    }
}
