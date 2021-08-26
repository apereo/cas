package org.apereo.cas.oidc.discovery.webfinger;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcWebFingerUserInfoRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OIDC")
public class OidcWebFingerUserInfoRepositoryTests {

    @Test
    public void verifyOperation() {
        val repo = mock(OidcWebFingerUserInfoRepository.class);
        when(repo.findByEmailAddress(anyString())).thenCallRealMethod();
        when(repo.findByUsername(anyString())).thenCallRealMethod();
        when(repo.getName()).thenCallRealMethod();

        assertTrue(repo.findByEmailAddress("cas").isEmpty());
        assertTrue(repo.findByUsername("cas").isEmpty());
        assertNotNull(repo.getName());
    }

}
