package org.apereo.cas.audit.spi;

import org.apereo.cas.audit.spi.principal.DefaultAuditPrincipalIdProvider;
import org.apereo.cas.audit.spi.principal.ThreadLocalAuditPrincipalResolver;
import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link ThreadLocalAuditPrincipalResolver}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Tag("Audits")
public class ThreadLocalAuditPrincipalResolverTests {

    private final ThreadLocalAuditPrincipalResolver theResolver =
        new ThreadLocalAuditPrincipalResolver(new DefaultAuditPrincipalIdProvider());

    @AfterEach
    public void cleanup() {
        AuthenticationCredentialsThreadLocalBinder.clear();
    }

    @Test
    public void noAuthenticationOrCredentialsAvailableInThreadLocal() {
        assertResolvedPrincipal(PrincipalResolver.UNKNOWN_USER);
    }

    @Test
    public void singleThreadSetsSingleCredential() {
        AuthenticationCredentialsThreadLocalBinder.bindCurrent(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        assertResolvedPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME);
    }

    @Test
    public void singleThreadSetsMultipleCredentials() {
        AuthenticationCredentialsThreadLocalBinder.bindCurrent(
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("test2"));

        assertResolvedPrincipal(String.format("%s, %s", CoreAuthenticationTestUtils.CONST_USERNAME, "test2"));
    }

    @Test
    public void singleThreadSetsAuthentication() {
        AuthenticationCredentialsThreadLocalBinder.bindCurrent(CoreAuthenticationTestUtils.getAuthentication());
        assertResolvedPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME);
    }

    private void assertResolvedPrincipal(final String principalId) {
        assertEquals(principalId, theResolver.resolveFrom(null, (Object) null));
        assertEquals(PrincipalResolver.UNKNOWN_USER, theResolver.resolve());
    }
}
