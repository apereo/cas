package org.apereo.cas.acme;

import module java.base;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        mockMvc.perform(get("/.well-known/acme-challenge/token"))
            .andExpect(status().isOk())
            .andExpect(content().string("challenge"));
        assertNotNull(acmeChallengeRepository.get("token"));
        Awaitility.await().untilAsserted(() -> acmeChallengeRepository.get("token"));
    }
}
