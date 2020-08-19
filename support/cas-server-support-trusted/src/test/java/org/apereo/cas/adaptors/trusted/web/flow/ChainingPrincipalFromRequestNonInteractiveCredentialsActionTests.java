package org.apereo.cas.adaptors.trusted.web.flow;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ChainingPrincipalFromRequestNonInteractiveCredentialsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowActions")
public class ChainingPrincipalFromRequestNonInteractiveCredentialsActionTests extends BaseNonInteractiveCredentialsActionTests {
    @Autowired
    @Qualifier("remoteUserAuthenticationAction")
    private PrincipalFromRequestExtractorAction remoteUserAuthenticationAction;

    @Test
    public void verifyOperation() {
        val request = new MockHttpServletRequest();
        val principal = mock(Principal.class);
        when(principal.getName()).thenReturn("casuser");
        request.setUserPrincipal(principal);
        assertNotNull(remoteUserAuthenticationAction.getRemotePrincipalId(request));
    }
}
