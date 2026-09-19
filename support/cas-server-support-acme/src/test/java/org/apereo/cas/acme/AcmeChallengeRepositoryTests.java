package org.apereo.cas.acme;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AcmeChallengeRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 * @deprecated since 7.3.0
 */
@Tag("Web")
@SuppressWarnings("removal")
@Deprecated(since = "7.3.0", forRemoval = true)
class AcmeChallengeRepositoryTests extends BaseAcmeTests {

    /**
     * The repository drops a challenge two seconds after it was last accessed, so the assertion is
     * that the token is gone once it has been left alone for longer than that. The quiet period
     * ahead of the first check is the subject of the test rather than a wait for something to
     * happen: reading the token counts as an access, so polling for its absence would keep renewing
     * the very entry the test is waiting on and it would never expire.
     *
     * @throws Throwable in case of failure
     */
    @Test
    void verifyOperation() throws Throwable {
        acmeChallengeRepository.add("token", "challenge");
        assertNotNull(acmeChallengeRepository.get("token"));
        await().pollDelay(Duration.ofSeconds(3))
            .atMost(Duration.ofSeconds(30))
            .until(() -> acmeChallengeRepository.get("token") == null);
    }

}
