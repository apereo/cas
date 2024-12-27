package org.apereo.cas.impl.token;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InMemoryPasswordlessTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("PasswordOps")
class InMemoryPasswordlessTokenRepositoryTests extends BasePasswordlessUserAccountStoreTests {
    private static final String CAS_USER = "casuser";

    @Autowired
    @Qualifier(PasswordlessTokenRepository.BEAN_NAME)
    private PasswordlessTokenRepository passwordlessTokenRepository;

    @Test
    void verifyToken() {
        passwordlessTokenRepository.clean();

        val passwordlessUserAccount = PasswordlessUserAccount.builder().username(CAS_USER).build();
        val passwordlessRequest = PasswordlessAuthenticationRequest.builder().username(CAS_USER).build();
        val token = passwordlessTokenRepository.createToken(passwordlessUserAccount, passwordlessRequest);
        assertTrue(passwordlessTokenRepository.findToken(CAS_USER).isEmpty());

        val savedToken = passwordlessTokenRepository.saveToken(passwordlessUserAccount, passwordlessRequest, token);
        assertTrue(passwordlessTokenRepository.findToken(CAS_USER).isPresent());

        passwordlessTokenRepository.deleteToken(savedToken);
        assertTrue(passwordlessTokenRepository.findToken(CAS_USER).isEmpty());
    }
}
