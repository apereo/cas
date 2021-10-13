package org.apereo.cas.gauth;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.gauth.credential.DummyCredentialRepository;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorAccount;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorOneTimeTokenCredentialValidator;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.gauth.credential.InMemoryGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.token.GoogleAuthenticatorToken;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.CachingOneTimeTokenRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.support.WebUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountNotFoundException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GoogleAuthenticatorAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("MFAProvider")
public class GoogleAuthenticatorAuthenticationHandlerTests {
    private IGoogleAuthenticator googleAuthenticator;

    private GoogleAuthenticatorAuthenticationHandler handler;

    private GoogleAuthenticatorKey account;

    private OneTimeTokenRepository tokenRepository;

    private OneTimeTokenCredentialRepository tokenCredentialRepository;

    @BeforeEach
    public void initialize() {
        val servicesManager = mock(ServicesManager.class);
        val builder = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder();
        googleAuthenticator = new GoogleAuthenticator(builder.build());
        tokenRepository = new CachingOneTimeTokenRepository(Caffeine.newBuilder().initialCapacity(10).build(s -> null));
        tokenCredentialRepository = new InMemoryGoogleAuthenticatorTokenCredentialRepository(
            CipherExecutor.noOpOfStringToString(), googleAuthenticator);
        googleAuthenticator.setCredentialRepository(new DummyCredentialRepository());
        handler = new GoogleAuthenticatorAuthenticationHandler("GAuth",
            servicesManager,
            PrincipalFactoryUtils.newPrincipalFactory(),
            new GoogleAuthenticatorOneTimeTokenCredentialValidator(googleAuthenticator, tokenRepository, tokenCredentialRepository),
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
    public void verifyAuthnAccountNotFound() {
        val credential = getGoogleAuthenticatorTokenCredential();
        assertThrows(AccountNotFoundException.class, () -> handler.authenticate(credential));
    }

    @Test
    public void verifyAuthnFailsTokenUsed() {
        val credential = getGoogleAuthenticatorTokenCredential();
        handler.getValidator().store(
            new GoogleAuthenticatorToken(Integer.valueOf(credential.getToken()), "casuser"));

        val toSave = OneTimeTokenAccount.builder()
            .username("casuser")
            .name(UUID.randomUUID().toString())
            .secretKey(account.getKey())
            .validationCode(account.getVerificationCode())
            .scratchCodes(account.getScratchCodes())
            .build();
        tokenCredentialRepository.save(toSave);
        credential.setAccountId(toSave.getId());
        assertThrows(AccountExpiredException.class, () -> handler.authenticate(credential));
    }

    @Test
    public void verifyAuthnTokenFound() throws Exception {
        val credential = getGoogleAuthenticatorTokenCredential();
        val toSave = GoogleAuthenticatorAccount.builder()
            .username("casuser")
            .name(UUID.randomUUID().toString())
            .secretKey(account.getKey())
            .validationCode(account.getVerificationCode())
            .scratchCodes(account.getScratchCodes())
            .build();
        credential.setAccountId(toSave.getId());
        tokenCredentialRepository.save(toSave);
        val result = handler.authenticate(credential);
        assertNotNull(result);
        assertNotNull(tokenRepository.get("casuser", Integer.valueOf(credential.getToken())));
    }

    @Test
    public void verifyAuthnTokenScratchCode() throws Exception {
        val credential = getGoogleAuthenticatorTokenCredential();
        val toSave = GoogleAuthenticatorAccount.builder()
            .username("casuser")
            .name(UUID.randomUUID().toString())
            .secretKey(account.getKey())
            .validationCode(account.getVerificationCode())
            .scratchCodes(account.getScratchCodes())
            .build();
        tokenCredentialRepository.save(toSave);
        credential.setAccountId(toSave.getId());
        credential.setToken(Integer.toString(account.getScratchCodes().get(0)));
        val result = handler.authenticate(credential);
        assertNotNull(result);
        val otp = Integer.valueOf(credential.getToken());
        assertNotNull(tokenRepository.get("casuser", otp));
        assertFalse(tokenCredentialRepository.get("casuser").iterator().next().getScratchCodes().contains(otp));
    }

    @Test
    public void verifyMultipleDevices() throws Exception {
        val credential = getGoogleAuthenticatorTokenCredential();
        for (var i = 0; i < 2; i++) {
            val toSave = GoogleAuthenticatorAccount.builder()
                .username("casuser")
                .name(String.format("deviceName-%s", i))
                .secretKey(account.getKey())
                .validationCode(account.getVerificationCode())
                .scratchCodes(account.getScratchCodes())
                .build();
            tokenCredentialRepository.save(toSave);
        }
        credential.setAccountId(null);
        assertThrows(PreventedException.class, () -> handler.authenticate(credential));

        val oneAcct = tokenCredentialRepository.get("casuser").iterator().next();
        credential.setAccountId(oneAcct.getId());
        val result = handler.authenticate(credential);
        assertNotNull(result);
    }

    @Test
    public void verifySingleDevicesNoAcctId() throws Exception {
        val credential = getGoogleAuthenticatorTokenCredential();
        val toSave = GoogleAuthenticatorAccount.builder()
            .username("casuser")
            .name(UUID.randomUUID().toString())
            .secretKey(account.getKey())
            .validationCode(account.getVerificationCode())
            .scratchCodes(account.getScratchCodes())
            .build();
        tokenCredentialRepository.save(toSave);
        credential.setAccountId(null);
        val result = handler.authenticate(credential);
        assertNotNull(result);
    }

    private GoogleAuthenticatorTokenCredential getGoogleAuthenticatorTokenCredential() {
        val credential = new GoogleAuthenticatorTokenCredential();
        account = googleAuthenticator.createCredentials("casuser");
        val key = googleAuthenticator.getTotpPassword(account.getKey());
        credential.setToken(Integer.toString(key));
        return credential;
    }
}
