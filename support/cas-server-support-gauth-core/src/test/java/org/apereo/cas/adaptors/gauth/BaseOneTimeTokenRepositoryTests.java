package org.apereo.cas.adaptors.gauth;

import org.apereo.cas.adaptors.gauth.token.GoogleAuthenticatorToken;
import org.apereo.cas.authentication.OneTimeToken;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.util.SchedulingUtils;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import javax.annotation.PostConstruct;

import static org.junit.Assert.*;

/**
 * This is {@link BaseOneTimeTokenRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public abstract class BaseOneTimeTokenRepositoryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    public static final String CASUSER = "casuser";

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    public abstract OneTimeTokenRepository getRepository();

    @Test
    public void verifyTokenSave() {
        val repository = getRepository();
        var token = (OneTimeToken) new GoogleAuthenticatorToken(1234, CASUSER);
        repository.store(token);
        assertTrue(repository.exists(CASUSER, 1234));
        token = repository.get(CASUSER, 1234);
        assertTrue(token.getId() > 0);
    }

    @Test
    public void verifyTokensWithUniqueIdsSave() {
        val repository = getRepository();
        val token = new GoogleAuthenticatorToken(1111, CASUSER);
        repository.store(token);

        val token2 = new GoogleAuthenticatorToken(5678, CASUSER);
        repository.store(token2);

        val t1 = repository.get(CASUSER, 1111);
        val t2 = repository.get(CASUSER, 5678);

        assertTrue(t1.getId() > 0);
        assertTrue(t2.getId() > 0);
        assertNotEquals(token.getId(), token2.getId());
        assertEquals(1111, (int) t1.getToken());
    }

    @TestConfiguration
    public static class BaseTestConfiguration {
        @Autowired
        protected ApplicationContext applicationContext;

        @PostConstruct
        public void init() {
            SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
        }
    }
}
