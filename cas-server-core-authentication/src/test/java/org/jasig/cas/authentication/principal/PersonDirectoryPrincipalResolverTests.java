package org.jasig.cas.authentication.principal;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.util.AuthTestUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link PersonDirectoryPrincipalResolver}.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class PersonDirectoryPrincipalResolverTests {

    @Test
    public void verifyNullPrincipal() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        final Principal p = resolver.resolve(() -> null);
        assertNull(p);

    }

    @Test
    public void verifyNullAttributes() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        resolver.setReturnNullIfNoAttributes(true);
        resolver.setPrincipalAttributeName(AuthTestUtils.CONST_USERNAME);
        final Credential c = AuthTestUtils.getCredentialsWithSameUsernameAndPassword();
        final Principal p = resolver.resolve(c);
        assertNull(p);
    }

    @Test
    public void verifyNoAttributesWithPrincipal() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        resolver.setPrincipalAttributeName(AuthTestUtils.CONST_USERNAME);
        final Credential c = AuthTestUtils.getCredentialsWithSameUsernameAndPassword();
        final Principal p = resolver.resolve(c);
        assertNotNull(p);
    }

    @Test
    public void verifyAttributesWithPrincipal() {
        final PersonDirectoryPrincipalResolver resolver = new PersonDirectoryPrincipalResolver();
        resolver.setAttributeRepository(AuthTestUtils.getAttributeRepository());
        resolver.setPrincipalAttributeName("cn");
        final Credential c = AuthTestUtils.getCredentialsWithSameUsernameAndPassword();
        final Principal p = resolver.resolve(c);
        assertNotNull(p);
        assertNotEquals(p.getId(), AuthTestUtils.CONST_USERNAME);
        assertTrue(p.getAttributes().containsKey("memberOf"));
    }

}
