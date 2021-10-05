package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.OneTimeToken;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.gauth.token.GoogleAuthenticatorToken;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialValidator;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.util.CollectionUtils;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountNotFoundException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GoogleAuthenticatorOneTimeTokenCredentialValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    GoogleAuthenticatorOneTimeTokenCredentialValidatorTests.GoogleAuthenticatorOneTimeTokenCredentialValidatorTestConfiguration.class,
    BaseGoogleAuthenticatorTests.SharedTestConfiguration.class
})
@Getter
@Tag("MFAProvider")
public class GoogleAuthenticatorOneTimeTokenCredentialValidatorTests {

    @Autowired
    @Qualifier("googleAuthenticatorOneTimeTokenCredentialValidator")
    private OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> validator;

    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry;

    @Autowired
    @Qualifier("oneTimeTokenAuthenticatorTokenRepository")
    private OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository;

    @Test
    public void verifyTokenAuthz() {
        val acct = OneTimeTokenAccount.builder()
            .username("casuser")
            .name(UUID.randomUUID().toString())
            .secretKey("secret")
            .validationCode(123456)
            .scratchCodes(List.of())
            .build();
        assertTrue(validator.isTokenAuthorizedFor(123456, acct));
        assertFalse(validator.isTokenAuthorizedFor(987654, acct));
    }

    @Test
    public void verifyStore() {
        val token = new GoogleAuthenticatorToken(632435, "casuser");
        assertDoesNotThrow(() -> validator.store(token));
    }

    @Test
    public void verifyAcctValidation() throws Exception {
        val acct = GoogleAuthenticatorAccount.builder()
            .username("casuser")
            .name(UUID.randomUUID().toString())
            .secretKey("secret")
            .validationCode(123456)
            .scratchCodes(List.of())
            .build();
        googleAuthenticatorAccountRegistry.save(acct);

        val cred = new GoogleAuthenticatorTokenCredential("123456", acct.getId());
        assertNotNull(validator.validate(CoreAuthenticationTestUtils.getAuthentication(acct.getUsername()), cred));
    }

    @Test
    public void verifyAcctValidationScratchCode() throws Exception {
        val acct = GoogleAuthenticatorAccount.builder()
            .username("casuser")
            .name(UUID.randomUUID().toString())
            .secretKey("secret")
            .validationCode(123456)
            .scratchCodes(CollectionUtils.wrapList(834251))
            .build();
        googleAuthenticatorAccountRegistry.save(acct);

        val cred = new GoogleAuthenticatorTokenCredential("834251", acct.getId());
        assertNotNull(validator.validate(CoreAuthenticationTestUtils.getAuthentication(acct.getUsername()), cred));
        assertTrue(googleAuthenticatorAccountRegistry.get(acct.getId()).getScratchCodes().isEmpty());
    }

    @Test
    public void verifyTokenReuse() {
        val acct = GoogleAuthenticatorAccount.builder()
            .username("casuser")
            .name(UUID.randomUUID().toString())
            .secretKey("secret")
            .validationCode(123456)
            .scratchCodes(List.of())
            .build();
        googleAuthenticatorAccountRegistry.save(acct);

        val otp1 = new OneTimeToken(556644, "casuser");
        oneTimeTokenAuthenticatorTokenRepository.store(otp1);
        assertThrows(AccountExpiredException.class,
            () -> validator.validate(CoreAuthenticationTestUtils.getAuthentication("casuser"),
                new GoogleAuthenticatorTokenCredential("556644", 123456L)));
    }

    @Test
    public void verifyBadToken() {
        assertThrows(PreventedException.class,
            () -> validator.validate(CoreAuthenticationTestUtils.getAuthentication("casuser"),
                new GoogleAuthenticatorTokenCredential("abcdefg", 123456L)));
        assertThrows(AccountNotFoundException.class,
            () -> validator.validate(CoreAuthenticationTestUtils.getAuthentication("unknown-user"),
                new GoogleAuthenticatorTokenCredential("112233", 123456L)));
    }

    @Test
    public void verifyMultipleAccountsWithNoId() {
        for (var i = 0; i < 2; i++) {
            val acct = GoogleAuthenticatorAccount.builder()
                .username("casuser")
                .name(String.format("account-%s", i))
                .secretKey("secret")
                .validationCode(123456)
                .scratchCodes(List.of(222222, 333333))
                .build();
            googleAuthenticatorAccountRegistry.save(acct);
        }
        val cred = new GoogleAuthenticatorTokenCredential("112233", null);
        assertThrows(PreventedException.class,
            () -> validator.validate(CoreAuthenticationTestUtils.getAuthentication("casuser"), cred));
    }

    @TestConfiguration("GoogleAuthenticatorOneTimeTokenCredentialValidatorTestConfiguration")
    public static class GoogleAuthenticatorOneTimeTokenCredentialValidatorTestConfiguration {
        @Bean
        public IGoogleAuthenticator googleAuthenticatorInstance() {
            val auth = mock(IGoogleAuthenticator.class);
            when(auth.authorize(anyString(), ArgumentMatchers.eq(123456))).thenReturn(Boolean.TRUE);
            when(auth.authorize(anyString(), ArgumentMatchers.eq(987654))).thenReturn(Boolean.FALSE);
            when(auth.authorize(anyString(), ArgumentMatchers.eq(112233))).thenThrow(new IllegalArgumentException());
            return auth;
        }
    }
}
