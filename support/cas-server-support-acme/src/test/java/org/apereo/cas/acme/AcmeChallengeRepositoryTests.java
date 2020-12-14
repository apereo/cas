package org.apereo.cas.acme;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AcmeChallengeRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Web")
public class AcmeChallengeRepositoryTests extends BaseAcmeTests {

    @Test
    public void verifyOperation() throws Exception {
        acmeChallengeRepository.add("token", "challenge");
        assertNotNull(acmeChallengeRepository.get("token"));
        Thread.sleep(3000);
        assertNull(acmeChallengeRepository.get("token"));
    }

}
