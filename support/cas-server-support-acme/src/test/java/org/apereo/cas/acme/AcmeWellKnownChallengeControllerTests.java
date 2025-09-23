package org.apereo.cas.acme;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AcmeWellKnownChallengeControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 * @deprecated since 7.3.0
 */
@Tag("Web")
@SuppressWarnings("removal")
@Deprecated(since = "7.3.0", forRemoval = true)
class AcmeWellKnownChallengeControllerTests extends BaseAcmeTests {

    @Test
    void verifyOperation() throws Throwable {
        acmeChallengeRepository.add("token", "challenge");
        assertNotNull(acmeWellKnownChallengeController.handleRequest("token",
            new MockHttpServletRequest(), new MockHttpServletResponse()));
        assertNotNull(acmeChallengeRepository.get("token"));
        Thread.sleep(3000);
        assertNull(acmeChallengeRepository.get("token"));
    }
}
