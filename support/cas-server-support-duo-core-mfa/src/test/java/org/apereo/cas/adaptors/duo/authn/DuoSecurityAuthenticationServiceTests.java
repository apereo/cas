package org.apereo.cas.adaptors.duo.authn;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFA")
public class DuoSecurityAuthenticationServiceTests {

    @Test
    public void verifyOperation() {
        val service = mock(DuoSecurityAuthenticationService.class);
        when(service.getDuoClient()).thenCallRealMethod();
        when(service.signRequestToken(anyString())).thenCallRealMethod();
        assertTrue(service.getDuoClient().isEmpty());
        assertTrue(service.signRequestToken("anything").isEmpty());
    }

}
