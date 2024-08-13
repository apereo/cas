package org.apereo.cas.gauth.token;

import org.apereo.cas.authentication.OneTimeToken;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepositoryCleaner;
import org.apereo.cas.util.RandomUtils;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.Locale;
import java.util.UUID;
import static org.awaitility.Awaitility.*;
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
    void verifyTokenSave() throws Throwable {
        val otp = getRandomOtp();
        val token = oneTimeTokenAuthenticatorTokenRepository.store(new GoogleAuthenticatorToken(otp, userId));
        assertNotNull(token);
        assertTrue(oneTimeTokenAuthenticatorTokenRepository.exists(userId, otp));
        val foundToken = oneTimeTokenAuthenticatorTokenRepository.get(userId, otp);
        assertTrue(foundToken.getId() > 0);
        oneTimeTokenAuthenticatorTokenRepository.clean();
        googleAuthenticatorTokenRepositoryCleaner.clean();
    }

    @RetryingTest(3)
    void verifyCaseInsensitiveUser() throws Throwable {
        val otp = getRandomOtp();
        val token = (OneTimeToken) new GoogleAuthenticatorToken(otp, userId.toUpperCase(Locale.ENGLISH));
        oneTimeTokenAuthenticatorTokenRepository.store(token);
        await().untilAsserted(() -> assertTrue(oneTimeTokenAuthenticatorTokenRepository.exists(userId.toLowerCase(Locale.ENGLISH), otp)));
        await().untilAsserted(() -> assertNotNull(oneTimeTokenAuthenticatorTokenRepository.get(userId.toLowerCase(Locale.ENGLISH), otp)));
    }

    @Test
    void verifyTokensWithUniqueIdsSave() throws Throwable {
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
        assertNotEquals(t1.getId(), t2.getId());
        assertEquals(otp1, (int) t1.getToken());
    }

    @Test
    void verifyRemoveByUserAndCode() throws Throwable {
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
    void verifyRemoveByUser() throws Throwable {
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
    void verifyRemoveByCode() throws Throwable {
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
    void verifySize() throws Throwable {
        oneTimeTokenAuthenticatorTokenRepository.removeAll();
        assertEquals(0, oneTimeTokenAuthenticatorTokenRepository.count(), "Repository is not empty");
        assertEquals(0, oneTimeTokenAuthenticatorTokenRepository.count());
        val uid = UUID.randomUUID().toString();
        val otp = getRandomOtp();
        val token = new GoogleAuthenticatorToken(otp, uid);
        oneTimeTokenAuthenticatorTokenRepository.store(token);
        assertTrue(oneTimeTokenAuthenticatorTokenRepository.count(uid) > 0);
    }
}
