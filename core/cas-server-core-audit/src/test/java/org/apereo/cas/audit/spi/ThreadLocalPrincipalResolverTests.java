package org.apereo.cas.audit.spi;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.AuthenticationCredentialsLocalBinder;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for {@link ThreadLocalPrincipalResolver}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class ThreadLocalPrincipalResolverTests {

    private final ThreadLocalPrincipalResolver theResolver =
            new ThreadLocalPrincipalResolver(new AuditPrincipalIdProvider() {});

    @After
    public void cleanup() {
        AuthenticationCredentialsLocalBinder.clear();
    }

    @Test
    public void noAuthenticationOrCrendentialsAvailableInThreadLocal() {
        assertResolvedPrincipal(PrincipalResolver.UNKNOWN_USER);
    }

    @Test
    public void singleThreadSetsSingleCredential() {
        AuthenticationCredentialsLocalBinder.bindCurrent(CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        assertResolvedPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME);
    }

    @Test
    public void singleThreadSetsMultipleCredentials() {
        AuthenticationCredentialsLocalBinder.bindCurrent(
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("test2"));

        assertResolvedPrincipal(String.format("%s, %s", CoreAuthenticationTestUtils.CONST_USERNAME, "test2"));
    }

    @Test
    public void singleThreadSetsAuthentication() {
        AuthenticationCredentialsLocalBinder.bindCurrent(CoreAuthenticationTestUtils.getAuthentication());
        assertResolvedPrincipal(CoreAuthenticationTestUtils.CONST_USERNAME);
    }

    private void assertResolvedPrincipal(final String principalId) {
        assertEquals(principalId, theResolver.resolveFrom(null, (Object) null));
    }
}
