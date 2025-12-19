package org.apereo.cas.impl.tokens;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.impl.BaseMongoDbPasswordlessTests;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoDbPasswordlessTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("MongoDb")
@EnabledIfListeningOnPort(port = 27017)
class MongoDbPasswordlessTokenRepositoryTests extends BaseMongoDbPasswordlessTests {
    @Test
    void verifyAction() {
        val uid = UUID.randomUUID().toString();

        val passwordlessUserAccount = PasswordlessUserAccount.builder().username(uid).build();
        val passwordlessRequest = PasswordlessAuthenticationRequest.builder().username(uid).build();
        val token = passwordlessTokenRepository.createToken(passwordlessUserAccount, passwordlessRequest);

        assertTrue(passwordlessTokenRepository.findToken(uid).isEmpty());

        val savedToken = passwordlessTokenRepository.saveToken(passwordlessUserAccount, passwordlessRequest, token);
        assertTrue(passwordlessTokenRepository.findToken(uid).isPresent());

        passwordlessTokenRepository.deleteToken(savedToken);
        assertTrue(passwordlessTokenRepository.findToken(uid).isEmpty());
    }


    @Test
    void verifyCleaner() {
        val uid = UUID.randomUUID().toString();
        val passwordlessUserAccount = PasswordlessUserAccount.builder().username(uid).build();
        val passwordlessRequest = PasswordlessAuthenticationRequest.builder().username(uid).build();
        val token = passwordlessTokenRepository.createToken(passwordlessUserAccount, passwordlessRequest);

        passwordlessTokenRepository.saveToken(passwordlessUserAccount, passwordlessRequest, token);
        assertTrue(passwordlessTokenRepository.findToken(uid).isPresent());

        passwordlessTokenRepository.clean();

        assertTrue(passwordlessTokenRepository.findToken(uid).isEmpty());
    }
}
