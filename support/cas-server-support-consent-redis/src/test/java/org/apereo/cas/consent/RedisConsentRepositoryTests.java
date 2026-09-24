package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.config.CasConsentRedisAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RedisConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasConsentRedisAutoConfiguration.class,
    BaseConsentRepositoryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.consent.redis.host=localhost",
        "cas.consent.redis.port=6379",
        "cas.consent.redis.pool.max-active=20",
        "cas.consent.redis.pool.enabled=true"
    })
@Tag("Redis")
@ExtendWith(CasTestExtension.class)
@Getter
@EnabledIfListeningOnPort(port = 6379)
class RedisConsentRepositoryTests extends BaseConsentRepositoryTests {

    @Autowired
    @Qualifier(ConsentRepository.BEAN_NAME)
    protected ConsentRepository repository;

    @Test
    void storeBadDecision() throws Throwable {
        val repo = getRepository();
        assertNull(repo.storeConsentDecision(null));
    }
    
    @Test
    void verifyDeleteFails() throws Throwable {
        val repo = getRepository();
        assertFalse(repo.deleteConsentDecision(-1, UUID.randomUUID().toString()));
    }

    @Test
    void verifyPrincipalIsMatchedLiterally() throws Throwable {
        val repo = getRepository();
        val user = getUser();
        val nestedUser = user + ":nested";
        assertNotNull(repo.storeConsentDecision(BUILDER.build(SVC, REG_SVC, user, ATTR)));
        assertNotNull(repo.storeConsentDecision(BUILDER.build(SVC, REG_SVC, nestedUser, ATTR)));

        assertEquals(1, repo.findConsentDecisions(user).size());
        assertEquals(1, repo.findConsentDecisions(nestedUser).size());
        assertTrue(repo.findConsentDecisions(user.substring(0, 4) + '*').isEmpty());
        assertTrue(repo.findConsentDecisions(user.substring(0, 7) + '?').isEmpty());
        assertTrue(repo.findConsentDecisions('[' + user.substring(0, 1) + ']' + user.substring(1)).isEmpty());
        assertFalse(repo.deleteConsentDecisions(user.substring(0, 4) + '*'));
        assertEquals(1, repo.findConsentDecisions(user).size());

        assertTrue(repo.deleteConsentDecisions(user));
        assertTrue(repo.findConsentDecisions(user).isEmpty());
        assertEquals(1, repo.findConsentDecisions(nestedUser).size());
    }
}
