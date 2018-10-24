package org.apereo.cas.audit.spi;

import org.apereo.cas.audit.spi.principal.DefaultAuditPrincipalIdProvider;
import org.apereo.cas.audit.spi.principal.ThreadLocalPrincipalResolver;
import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

/**
 * Unit test for {@link ThreadLocalPrincipalResolver}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class ThreadLocalPrincipalResolverTests {

    private final ThreadLocalPrincipalResolver theResolver =
        new ThreadLocalPrincipalResolver(new DefaultAuditPrincipalIdProvider());

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
    }
}
