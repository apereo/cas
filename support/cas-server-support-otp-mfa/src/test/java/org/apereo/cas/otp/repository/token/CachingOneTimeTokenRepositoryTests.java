package org.apereo.cas.otp.repository.token;

import org.apereo.cas.authentication.OneTimeToken;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CachingOneTimeTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseOneTimeTokenRepositoryTests.SharedTestConfiguration.class)
@Getter
@Tag("MFA")
public class CachingOneTimeTokenRepositoryTests extends BaseOneTimeTokenRepositoryTests {

    @Autowired
    @Qualifier("oneTimeTokenAuthenticatorTokenRepository")
    private OneTimeTokenRepository repository;

    @Test
    public void verifyOperation() {
        var token = (OneTimeToken) new OneTimeToken(1234, CASUSER);
        repository.store(token);
        repository.remove(token.getUserId(), token.getToken());
        assertFalse(repository.exists(token.getUserId(), token.getToken()));

        repository.removeAll();
        assertEquals(0, repository.count());
    }

}
