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

    private static int getRandomOtp() {
        return RandomUtils.nextInt(10000, 99999);
    }

    @BeforeEach
    public void initialize() {
        this.userId = RandomUtils.randomAlphabetic(6);
    }

    @Test
    public void verifyTokenSave() {
        val otp = getRandomOtp();
        var token = (OneTimeToken) new GoogleAuthenticatorToken(otp, userId);
        oneTimeTokenAuthenticatorTokenRepository.store(token);
        assertTrue(oneTimeTokenAuthenticatorTokenRepository.exists(userId, otp));
        token = oneTimeTokenAuthenticatorTokenRepository.get(userId, otp);
        assertTrue(token.getId() > 0);
        oneTimeTokenAuthenticatorTokenRepository.clean();
        googleAuthenticatorTokenRepositoryCleaner.clean();
    }

    @Test
    public void verifyCaseInsensitiveUser() {
        val otp = getRandomOtp();
        var token = (OneTimeToken) new GoogleAuthenticatorToken(otp, userId.toUpperCase());
        oneTimeTokenAuthenticatorTokenRepository.store(token);
        assertTrue(oneTimeTokenAuthenticatorTokenRepository.exists(userId.toLowerCase(), otp));
        token = oneTimeTokenAuthenticatorTokenRepository.get(userId.toLowerCase(), otp);
        assertTrue(token.getId() > 0);
    }

    @Test
    public void verifyTokensWithUniqueIdsSave() {
        val otp1 = getRandomOtp();
        val token = new GoogleAuthenticatorToken(otp1, userId);
        oneTimeTokenAuthenticatorTokenRepository.store(token);

        val otp2 = getRandomOtp();
        val token2 = new GoogleAuthenticatorToken(otp2, userId);
        oneTimeTokenAuthenticatorTokenRepository.store(token2);

        val t1 = oneTimeTokenAuthenticatorTokenRepository.get(userId, token.getToken());
        val t2 = oneTimeTokenAuthenticatorTokenRepository.get(userId, token2.getToken());

        assertTrue(t1.getId() > 0);
        assertTrue(t2.getId() > 0);
        assertNotEquals(token.getId(), token2.getId());
        assertEquals(otp1, (int) t1.getToken());
    }

    @Test
    public void verifyRemoveByUserAndCode() {
        val otp = getRandomOtp();
        val token = new GoogleAuthenticatorToken(otp, userId);
        oneTimeTokenAuthenticatorTokenRepository.store(token);
        var newToken = oneTimeTokenAuthenticatorTokenRepository.get(userId, token.getToken());
        assertNotNull(newToken);
        assertTrue(newToken.getId() > 0);
        oneTimeTokenAuthenticatorTokenRepository.remove(userId, otp);
        newToken = oneTimeTokenAuthenticatorTokenRepository.get(userId, otp);
        assertNull(newToken);
    }

    @Test
    public void verifyRemoveByUser() {
        val otp = getRandomOtp();
        val token = new GoogleAuthenticatorToken(otp, userId);
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
        val otp = getRandomOtp();
        val token = new GoogleAuthenticatorToken(otp, "someone");
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
        val otp = getRandomOtp();
        assertEquals(oneTimeTokenAuthenticatorTokenRepository.count(), 0);
        val token = new GoogleAuthenticatorToken(otp, uid);
        oneTimeTokenAuthenticatorTokenRepository.store(token);
        assertEquals(1, oneTimeTokenAuthenticatorTokenRepository.count());
        assertEquals(1, oneTimeTokenAuthenticatorTokenRepository.count(uid));
        oneTimeTokenAuthenticatorTokenRepository.removeAll();
        assertEquals(0, oneTimeTokenAuthenticatorTokenRepository.count(), "Repository is not empty");
    }
}
