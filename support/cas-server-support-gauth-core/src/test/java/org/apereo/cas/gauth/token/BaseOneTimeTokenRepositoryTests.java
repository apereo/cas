package org.apereo.cas.gauth.token;

import module java.base;
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
    @Qualifier(OneTimeTokenRepository.BEAN_NAME)
    protected OneTimeTokenRepository oneTimeTokenAuthenticatorTokenRepository;

    @Autowired
    @Qualifier("googleAuthenticatorTokenRepositoryCleaner")
    protected OneTimeTokenRepositoryCleaner googleAuthenticatorTokenRepositoryCleaner;

    private static int getRandomOtp() {
        return RandomUtils.nextInt(10000, 99999);
    }

    @BeforeEach
    void initialize() {
        this.userId = RandomUtils.randomAlphabetic(6);
    }

    /**
     * Saving a token must make it findable by its user and code. The repository-wide cleaners are
     * deliberately not called from here: they remove every expired token in the repository, which
     * is the fixture a sibling test builds on purpose, and this test asserted nothing about them.
     */
    @Test
    void verifyTokenSave() {
        val otp = getRandomOtp();
        val token = oneTimeTokenAuthenticatorTokenRepository.store(new GoogleAuthenticatorToken(otp, userId));
        assertNotNull(token);
        assertTrue(oneTimeTokenAuthenticatorTokenRepository.exists(userId, otp));
        val foundToken = oneTimeTokenAuthenticatorTokenRepository.get(userId, otp);
        assertTrue(foundToken.getId() > 0);
    }

    @RetryingTest(3)
    void verifyCaseInsensitiveUser() {
        val otp = getRandomOtp();
        val token = (OneTimeToken) new GoogleAuthenticatorToken(otp, userId.toUpperCase(Locale.ENGLISH));
        oneTimeTokenAuthenticatorTokenRepository.store(token);
        await().untilAsserted(() -> assertTrue(oneTimeTokenAuthenticatorTokenRepository.exists(userId.toLowerCase(Locale.ENGLISH), otp)));
        await().untilAsserted(() -> assertNotNull(oneTimeTokenAuthenticatorTokenRepository.get(userId.toLowerCase(Locale.ENGLISH), otp)));
    }

    @Test
    void verifyTokensWithUniqueIdsSave() {
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
    void verifyRemoveByUserAndCode() {
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
    void verifyRemoveByUser() {
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
    void verifyRemoveByCode() {
        val otp = getRandomOtp();
        val token = new GoogleAuthenticatorToken(otp, userId);
        oneTimeTokenAuthenticatorTokenRepository.store(token);
        var newToken = oneTimeTokenAuthenticatorTokenRepository.get(token.getUserId(), token.getToken());
        assertNotNull(newToken);
        assertTrue(newToken.getId() > 0);
        oneTimeTokenAuthenticatorTokenRepository.remove(token.getToken());
        newToken = oneTimeTokenAuthenticatorTokenRepository.get(token.getUserId(), token.getToken());
        assertNull(newToken);
    }

    /**
     * The count for a user must reflect what has been stored for that user. It is asserted for a
     * user of this test's own making rather than for the repository as a whole, because a
     * repository-wide count answers for every test sharing the repository, and emptying it first to
     * make that count predictable would take away what those tests are holding.
     */
    @Test
    void verifySize() {
        val uid = UUID.randomUUID().toString();
        assertEquals(0, oneTimeTokenAuthenticatorTokenRepository.count(uid), "Repository is not empty for user");
        val otp = getRandomOtp();
        val token = new GoogleAuthenticatorToken(otp, uid);
        oneTimeTokenAuthenticatorTokenRepository.store(token);
        assertTrue(oneTimeTokenAuthenticatorTokenRepository.count(uid) > 0);
    }
}
