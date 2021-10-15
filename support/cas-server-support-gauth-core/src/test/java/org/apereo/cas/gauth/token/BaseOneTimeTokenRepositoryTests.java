package org.apereo.cas.gauth.token;

import org.apereo.cas.authentication.OneTimeToken;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepositoryCleaner;
import org.apereo.cas.util.RandomUtils;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseOneTimeTokenRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
public abstract class BaseOneTimeTokenRepositoryTests {
    protected String userId;

    @Autowired
    @Qualifier("oneTimeTokenAuthenticatorTokenRepository")
    protected OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository;

    @Autowired
    @Qualifier("googleAuthenticatorTokenRepositoryCleaner")
    protected OneTimeTokenRepositoryCleaner googleAuthenticatorTokenRepositoryCleaner;

    @BeforeEach
    public void initialize() {
        this.userId = RandomUtils.randomAlphabetic(6);
    }

    @Test
    public void verifyTokenSave() {
        var token = (OneTimeToken) new GoogleAuthenticatorToken(1234, userId);
        oneTimeTokenAuthenticatorTokenRepository.store(token);
        assertTrue(oneTimeTokenAuthenticatorTokenRepository.exists(userId, 1234));
        token = oneTimeTokenAuthenticatorTokenRepository.get(userId, 1234);
        assertTrue(token.getId() > 0);
        oneTimeTokenAuthenticatorTokenRepository.clean();
        googleAuthenticatorTokenRepositoryCleaner.clean();
    }

    @Test
    public void verifyCaseInsensitiveUser() {
        var token = (OneTimeToken) new GoogleAuthenticatorToken(1234, userId.toUpperCase());
        oneTimeTokenAuthenticatorTokenRepository.store(token);
        assertTrue(oneTimeTokenAuthenticatorTokenRepository.exists(userId.toLowerCase(), 1234));
        token = oneTimeTokenAuthenticatorTokenRepository.get(userId.toLowerCase(), 1234);
        assertTrue(token.getId() > 0);
    }

    @Test
    public void verifyTokensWithUniqueIdsSave() {
        val token = new GoogleAuthenticatorToken(1111, userId);
        oneTimeTokenAuthenticatorTokenRepository.store(token);

        val token2 = new GoogleAuthenticatorToken(5678, userId);
        oneTimeTokenAuthenticatorTokenRepository.store(token2);

        val t1 = oneTimeTokenAuthenticatorTokenRepository.get(userId, token.getToken());
        val t2 = oneTimeTokenAuthenticatorTokenRepository.get(userId, token2.getToken());

        assertTrue(t1.getId() > 0);
        assertTrue(t2.getId() > 0);
        assertNotEquals(token.getId(), token2.getId());
        assertEquals(1111, (int) t1.getToken());
    }

    @Test
    public void verifyRemoveByUserAndCode() {
        val token = new GoogleAuthenticatorToken(1984, userId);
        oneTimeTokenAuthenticatorTokenRepository.store(token);
        var newToken = oneTimeTokenAuthenticatorTokenRepository.get(userId, token.getToken());
        assertNotNull(newToken);
        assertTrue(newToken.getId() > 0);
        oneTimeTokenAuthenticatorTokenRepository.remove(userId, 1984);
        newToken = oneTimeTokenAuthenticatorTokenRepository.get(userId, 1984);
        assertNull(newToken);
    }

    @Test
    public void verifyRemoveByUser() {
        val token = new GoogleAuthenticatorToken(61984, userId);
        oneTimeTokenAuthenticatorTokenRepository.store(token);
        var newToken = oneTimeTokenAuthenticatorTokenRepository.get(userId, token.getToken());
        assertNotNull(newToken);
        assertTrue(newToken.getId() > 0);
        oneTimeTokenAuthenticatorTokenRepository.remove(userId);
        newToken = oneTimeTokenAuthenticatorTokenRepository.get(userId, token.getToken());
        assertNull(newToken);
    }

    @Test
    public void verifyRemoveByCode() {
        val token = new GoogleAuthenticatorToken(51984, "someone");
        oneTimeTokenAuthenticatorTokenRepository.store(token);
        var newToken = oneTimeTokenAuthenticatorTokenRepository.get(token.getUserId(), token.getToken());
        assertNotNull(newToken);
        assertTrue(newToken.getId() > 0);
        oneTimeTokenAuthenticatorTokenRepository.remove(token.getToken());
        newToken = oneTimeTokenAuthenticatorTokenRepository.get(token.getUserId(), token.getToken());
        assertNull(newToken);
    }

    @Test
    public void verifySize() {
        val uid = UUID.randomUUID().toString();
        assertEquals(oneTimeTokenAuthenticatorTokenRepository.count(), 0);
        val token = new GoogleAuthenticatorToken(916984, uid);
        oneTimeTokenAuthenticatorTokenRepository.store(token);
        assertEquals(1, oneTimeTokenAuthenticatorTokenRepository.count());
        assertEquals(1, oneTimeTokenAuthenticatorTokenRepository.count(uid));
        oneTimeTokenAuthenticatorTokenRepository.removeAll();
        assertEquals(0, oneTimeTokenAuthenticatorTokenRepository.count(), "Repository is not empty");
    }
}
