package org.apereo.cas.gauth.token;

import org.apereo.cas.authentication.OneTimeToken;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.util.SchedulingUtils;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseOneTimeTokenRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Tag("MFA")
public abstract class BaseOneTimeTokenRepositoryTests {
    public static final String CASUSER = "casuser";

    @Autowired
    @Qualifier("oneTimeTokenAuthenticatorTokenRepository")
    protected ObjectProvider<OneTimeTokenRepository> oneTimeTokenAuthenticatorTokenRepository;

    @Test
    public void verifyTokenSave() {
        var token = (OneTimeToken) new GoogleAuthenticatorToken(1234, CASUSER);
        oneTimeTokenAuthenticatorTokenRepository.getObject().store(token);
        assertTrue(oneTimeTokenAuthenticatorTokenRepository.getObject().exists(CASUSER, 1234));
        token = oneTimeTokenAuthenticatorTokenRepository.getObject().get(CASUSER, 1234);
        assertTrue(token.getId() > 0);
        oneTimeTokenAuthenticatorTokenRepository.getObject().clean();
    }

    @Test
    public void verifyTokensWithUniqueIdsSave() {
        val token = new GoogleAuthenticatorToken(1111, CASUSER);
        oneTimeTokenAuthenticatorTokenRepository.getObject().store(token);

        val token2 = new GoogleAuthenticatorToken(5678, CASUSER);
        oneTimeTokenAuthenticatorTokenRepository.getObject().store(token2);

        val t1 = oneTimeTokenAuthenticatorTokenRepository.getObject().get(CASUSER, token.getToken());
        val t2 = oneTimeTokenAuthenticatorTokenRepository.getObject().get(CASUSER, token2.getToken());

        assertTrue(t1.getId() > 0);
        assertTrue(t2.getId() > 0);
        assertNotEquals(token.getId(), token2.getId());
        assertEquals(1111, (int) t1.getToken());
    }

    @Test
    public void verifyRemoveByUserAndCode() {
        val token = new GoogleAuthenticatorToken(1984, CASUSER);
        oneTimeTokenAuthenticatorTokenRepository.getObject().store(token);
        var newToken = oneTimeTokenAuthenticatorTokenRepository.getObject().get(CASUSER, token.getToken());
        assertNotNull(newToken);
        assertTrue(newToken.getId() > 0);
        oneTimeTokenAuthenticatorTokenRepository.getObject().remove(CASUSER, 1984);
        newToken = oneTimeTokenAuthenticatorTokenRepository.getObject().get(CASUSER, 1984);
        assertNull(newToken);
    }

    @Test
    public void verifyRemoveByUser() {
        val token = new GoogleAuthenticatorToken(61984, CASUSER);
        oneTimeTokenAuthenticatorTokenRepository.getObject().store(token);
        var newToken = oneTimeTokenAuthenticatorTokenRepository.getObject().get(CASUSER, token.getToken());
        assertNotNull(newToken);
        assertTrue(newToken.getId() > 0);
        oneTimeTokenAuthenticatorTokenRepository.getObject().remove(CASUSER);
        newToken = oneTimeTokenAuthenticatorTokenRepository.getObject().get(CASUSER, token.getToken());
        assertNull(newToken);
    }

    @Test
    public void verifyRemoveByCode() {
        val token = new GoogleAuthenticatorToken(51984, "someone");
        oneTimeTokenAuthenticatorTokenRepository.getObject().store(token);
        var newToken = oneTimeTokenAuthenticatorTokenRepository.getObject().get(token.getUserId(), token.getToken());
        assertNotNull(newToken);
        assertTrue(newToken.getId() > 0);
        oneTimeTokenAuthenticatorTokenRepository.getObject().remove(token.getToken());
        newToken = oneTimeTokenAuthenticatorTokenRepository.getObject().get(token.getUserId(), token.getToken());
        assertNull(newToken);
    }

    @Test
    public void verifySize() {
        assertEquals(oneTimeTokenAuthenticatorTokenRepository.getObject().count(), 0);
        val token = new GoogleAuthenticatorToken(916984, "sample");
        oneTimeTokenAuthenticatorTokenRepository.getObject().store(token);
        assertEquals(1, oneTimeTokenAuthenticatorTokenRepository.getObject().count());
        assertEquals(1, oneTimeTokenAuthenticatorTokenRepository.getObject().count("sample"));
        oneTimeTokenAuthenticatorTokenRepository.getObject().removeAll();
        assertEquals(0, oneTimeTokenAuthenticatorTokenRepository.getObject().count(), "Repository is not empty");
    }

    @TestConfiguration("BaseOneTimeTokenRepositoryTestConfiguration")
    @Lazy(false)
    public static class BaseOneTimeTokenRepositoryTestConfiguration {
        @Autowired
        protected ApplicationContext applicationContext;

        @PostConstruct
        public void init() {
            SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
        }
    }
}
