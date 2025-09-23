package org.apereo.cas.otp.repository.token;

import org.apereo.cas.authentication.OneTimeToken;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.UUID;
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
@ExtendWith(CasTestExtension.class)
@ResourceLock(value = "repository", mode = ResourceAccessMode.READ_WRITE)
class CachingOneTimeTokenRepositoryTests extends BaseOneTimeTokenRepositoryTests {

    @Autowired
    @Qualifier(OneTimeTokenRepository.BEAN_NAME)
    private OneTimeTokenRepository repository;

    @Test
    void verifyTokenSave() {
        val casuser = UUID.randomUUID().toString();
        val token = new OneTimeToken(1234, casuser);
        repository.store(token);
        repository.store(token);
        assertEquals(2, repository.count(casuser));
        repository.clean();
        assertTrue(repository.exists(casuser, 1234));
        repository.remove(casuser);
        repository.remove(1234);
        repository.remove(casuser, 1234);
        assertNull(repository.get(casuser, 1234));
        assertEquals(0, repository.count());
    }

    @Test
    void verifyOperation() {
        val id = UUID.randomUUID().toString();
        val token = new OneTimeToken(RandomUtils.nextInt(), id);
        repository.store(token);
        repository.remove(token.getUserId(), token.getToken());
        assertFalse(repository.exists(token.getUserId(), token.getToken()));
        repository.removeAll();
        assertEquals(0, repository.count());
    }

}
