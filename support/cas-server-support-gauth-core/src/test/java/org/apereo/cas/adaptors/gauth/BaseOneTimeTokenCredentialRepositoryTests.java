package org.apereo.cas.adaptors.gauth;

import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.util.CollectionUtils;
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
 * This is {@link BaseOneTimeTokenCredentialRepositoryTests}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public abstract class BaseOneTimeTokenCredentialRepositoryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    public static final String NEW_SECRET = "newSecret";
    public static final String CASUSER = "casuser";
    public static final String SECRET = "secret";

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    public abstract OneTimeTokenCredentialRepository getRegistry();

    @Test
    public void verifySave() {
        val registry = getRegistry();
        registry.save("uid", SECRET, 143211, CollectionUtils.wrapList(1, 2, 3, 4, 5, 6));
        val s = registry.get("uid");
        assertEquals(SECRET, s.getSecretKey());
    }

    @Test
    public void verifySaveAndUpdate() {
        val registry = getRegistry();
        registry.save(CASUSER, SECRET, 222222, CollectionUtils.wrapList(1, 2, 3, 4, 5, 6));
        var s = registry.get(CASUSER);
        assertNotNull(s.getRegistrationDate());
        assertEquals(222222, s.getValidationCode());
        s.setSecretKey(NEW_SECRET);
        s.setValidationCode(999666);
        registry.update(s);
        s = registry.get(CASUSER);
        assertEquals(999666, s.getValidationCode());
        assertEquals(NEW_SECRET, s.getSecretKey());
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
