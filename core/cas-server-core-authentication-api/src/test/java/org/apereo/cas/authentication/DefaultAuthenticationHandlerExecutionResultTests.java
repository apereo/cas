package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.DefaultMessageDescriptor;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultAuthenticationHandlerExecutionResultTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AuthenticationHandler")
class DefaultAuthenticationHandlerExecutionResultTests {

    @Test
    void verifyOperation() {
        val credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        val res = new DefaultAuthenticationHandlerExecutionResult(new SimpleTestUsernamePasswordAuthenticationHandler(),
            credential, CollectionUtils.wrapList(new DefaultMessageDescriptor("code1")));
        assertFalse(res.getWarnings().isEmpty());
        res.clearWarnings();
        assertTrue(res.getWarnings().isEmpty());
    }

    @Test
    void verifySourceWithPrincipal() {
        val res = new DefaultAuthenticationHandlerExecutionResult("Handler1", CoreAuthenticationTestUtils.getPrincipal("casuser"));
        assertTrue(res.getWarnings().isEmpty());
        assertNotNull(res.getCredential());
        assertEquals(res.getCredential().getId(), res.getPrincipal().getId());
    }
    
}
