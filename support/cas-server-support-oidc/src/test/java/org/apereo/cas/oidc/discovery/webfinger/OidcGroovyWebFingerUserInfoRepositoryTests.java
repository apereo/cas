package org.apereo.cas.oidc.discovery.webfinger;

import module java.base;
import org.apereo.cas.oidc.discovery.webfinger.userinfo.OidcGroovyWebFingerUserInfoRepository;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcGroovyWebFingerUserInfoRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Groovy")
class OidcGroovyWebFingerUserInfoRepositoryTests {
    @Test
    void verifyFindByEmail() throws Throwable {
        val repo = new OidcGroovyWebFingerUserInfoRepository(new ClassPathResource("webfinger.groovy"));
        val results = repo.findByEmailAddress("cas@example.org");
        assertNotNull(results);
        assertTrue(results.containsKey("email"));
        assertEquals("cas@example.org", results.get("email"));
        repo.destroy();
    }

    @Test
    void verifyFindByUsername() throws Throwable {
        val repo = new OidcGroovyWebFingerUserInfoRepository(new ClassPathResource("webfinger.groovy"));
        val results = repo.findByUsername("cas");
        assertNotNull(results);
        assertTrue(results.containsKey("username"));
        assertEquals("cas", results.get("username"));
        repo.destroy();
    }
}
