package org.apereo.cas.authentication.audit;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.SurrogateAuthenticationException;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogateAuditPrincipalIdProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Simple")
public class SurrogateAuditPrincipalIdProviderTests {
    @Test
    public void verifyAction() {
        val p = new SurrogateAuditPrincipalIdProvider();
        assertEquals(Credential.UNKNOWN_ID, p.getPrincipalIdFrom(null, null, null));

        val auth = CoreAuthenticationTestUtils.getAuthentication(
            CoreAuthenticationTestUtils.getPrincipal(),
            CollectionUtils.wrap(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED, List.of("true"),
                SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL, List.of("principal"),
                SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER, List.of("surrogateUser"))
        );
        assertTrue(p.supports(auth, new Object(), new SurrogateAuthenticationException("error")));
        assertNotNull(p.getPrincipalIdFrom(auth, new Object(), new SurrogateAuthenticationException("error")));

    }
}
