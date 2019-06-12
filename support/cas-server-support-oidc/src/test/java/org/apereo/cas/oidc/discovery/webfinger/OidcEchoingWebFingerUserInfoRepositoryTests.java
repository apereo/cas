package org.apereo.cas.oidc.discovery.webfinger;

import org.apereo.cas.oidc.discovery.webfinger.userinfo.OidcEchoingWebFingerUserInfoRepository;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcEchoingWebFingerUserInfoRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OIDC")
public class OidcEchoingWebFingerUserInfoRepositoryTests {
    @Test
    public void verifyFindByEmail() {
        val repo = new OidcEchoingWebFingerUserInfoRepository();
        val results = repo.findByEmailAddress("cas@example.org");
        assertNotNull(results);
        assertTrue(results.containsKey("email"));
        assertEquals("cas@example.org", results.get("email"));
    }

    @Test
    public void verifyFindByUsername() {
        val repo = new OidcEchoingWebFingerUserInfoRepository();
        val results = repo.findByUsername("cas");
        assertNotNull(results);
        assertTrue(results.containsKey("username"));
        assertEquals("cas", results.get("username"));
    }
}
