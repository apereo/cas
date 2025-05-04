package org.apereo.cas.gauth;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gauth.credential.DummyCredentialRepository;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorAccount;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorOneTimeTokenCredentialValidator;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.gauth.credential.InMemoryGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.token.GoogleAuthenticatorToken;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.CachingOneTimeTokenRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.DirectObjectProvider;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.support.WebUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountNotFoundException;
import java.util.ArrayList;
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
@SpringBootTest(classes = {
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasCoreMultitenancyAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTestAutoConfigurations
@ExtendWith(CasTestExtension.class)
class GoogleAuthenticatorAuthenticationHandlerTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    private TenantExtractor tenantExtractor;
    
    private CasGoogleAuthenticator googleAuthenticator;

    private GoogleAuthenticatorAuthenticationHandler handler;

    private GoogleAuthenticatorKey account;

    private OneTimeTokenRepository tokenRepository;

    private OneTimeTokenCredentialRepository tokenCredentialRepository;

    @BeforeEach
    void initialize() throws Exception {
        val servicesManager = mock(ServicesManager.class);
        googleAuthenticator = new DefaultCasGoogleAuthenticator(casProperties, tenantExtractor);
        tokenRepository = new CachingOneTimeTokenRepository(Caffeine.newBuilder().initialCapacity(10).build(s -> null));
        tokenCredentialRepository = new InMemoryGoogleAuthenticatorTokenCredentialRepository(
            CipherExecutor.noOpOfStringToString(), CipherExecutor.noOpOfNumberToNumber(), googleAuthenticator);
        googleAuthenticator.setCredentialRepository(new DummyCredentialRepository());
        handler = new GoogleAuthenticatorAuthenticationHandler("GAuth",
            servicesManager,
            PrincipalFactoryUtils.newPrincipalFactory(),
            new GoogleAuthenticatorOneTimeTokenCredentialValidator(googleAuthenticator, tokenRepository, tokenCredentialRepository),
            null, new DirectObjectProvider<>(mock(MultifactorAuthenticationProvider.class)));

        val context = MockRequestContext.create().setClientInfo();
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication("casuser"), context);
    }

    @Test
    void verifySupports() {
        val credential = new GoogleAuthenticatorTokenCredential();
        assertTrue(handler.supports(credential));
        assertTrue(handler.supports(GoogleAuthenticatorTokenCredential.class));
    }

    @Test
    void verifyAuthnAccountNotFound() {
        val credential = getGoogleAuthenticatorTokenCredential();
        assertThrows(AccountNotFoundException.class, () -> handler.authenticate(credential, mock(Service.class)));
    }

    @Test
    void verifyAuthnFailsTokenUsed() {
        val credential = getGoogleAuthenticatorTokenCredential();
        handler.getValidator().store(
            new GoogleAuthenticatorToken(Integer.valueOf(credential.getToken()), "casuser"));

        val toSave = OneTimeTokenAccount.builder()
            .username("casuser")
            .name(UUID.randomUUID().toString())
            .secretKey(account.getKey())
            .validationCode(account.getVerificationCode())
            .scratchCodes(new ArrayList<>(account.getScratchCodes()))
            .build();
        tokenCredentialRepository.save(toSave);
        credential.setAccountId(toSave.getId());
        assertThrows(AccountExpiredException.class, () -> handler.authenticate(credential, mock(Service.class)));
    }

    @Test
    void verifyAuthnTokenFound() throws Throwable {
        val credential = getGoogleAuthenticatorTokenCredential();
        val toSave = GoogleAuthenticatorAccount.builder()
            .username("casuser")
            .name(UUID.randomUUID().toString())
            .secretKey(account.getKey())
            .validationCode(account.getVerificationCode())
            .scratchCodes(new ArrayList<>(account.getScratchCodes()))
            .build();
        tokenCredentialRepository.save(toSave);
        credential.setAccountId(toSave.getId());
        val result = handler.authenticate(credential, mock(Service.class));
        assertNotNull(result);
        assertNotNull(tokenRepository.get("casuser", Integer.valueOf(credential.getToken())));
    }

    @Test
    void verifyAuthnTokenScratchCode() throws Throwable {
        val credential = getGoogleAuthenticatorTokenCredential();
        val toSave = GoogleAuthenticatorAccount.builder()
            .username("casuser")
            .name(UUID.randomUUID().toString())
            .secretKey(account.getKey())
            .validationCode(account.getVerificationCode())
            .scratchCodes(new ArrayList<>(account.getScratchCodes()))
            .build();
        tokenCredentialRepository.save(toSave);
        credential.setAccountId(toSave.getId());
        credential.setToken(Integer.toString(account.getScratchCodes().getFirst()));
        val result = handler.authenticate(credential, mock(Service.class));
        assertNotNull(result);
        val otp = Integer.valueOf(credential.getToken());
        assertNotNull(tokenRepository.get("casuser", otp));
        assertFalse(tokenCredentialRepository.get("casuser").iterator().next().getScratchCodes().contains(otp));
    }

    @Test
    void verifyMultipleDevices() throws Throwable {
        val credential = getGoogleAuthenticatorTokenCredential();
        for (var i = 0; i < 2; i++) {
            val toSave = GoogleAuthenticatorAccount.builder()
                .username("casuser")
                .name(String.format("deviceName-%s", i))
                .secretKey(account.getKey())
                .validationCode(account.getVerificationCode())
                .scratchCodes(new ArrayList<>(account.getScratchCodes()))
                .build();
            tokenCredentialRepository.save(toSave);
        }
        credential.setAccountId(null);
        assertThrows(PreventedException.class, () -> handler.authenticate(credential, mock(Service.class)));

        val oneAcct = tokenCredentialRepository.get("casuser").iterator().next();
        credential.setAccountId(oneAcct.getId());
        val result = handler.authenticate(credential, mock(Service.class));
        assertNotNull(result);
    }

    @Test
    void verifySingleDevicesNoAcctId() throws Throwable {
        val credential = getGoogleAuthenticatorTokenCredential();
        val toSave = GoogleAuthenticatorAccount.builder()
            .username("casuser")
            .name(UUID.randomUUID().toString())
            .secretKey(account.getKey())
            .validationCode(account.getVerificationCode())
            .scratchCodes(new ArrayList<>(account.getScratchCodes()))
            .build();
        tokenCredentialRepository.save(toSave);
        credential.setAccountId(null);
        val result = handler.authenticate(credential, mock(Service.class));
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
