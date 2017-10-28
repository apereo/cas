package org.apereo.cas.authentication;

import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * This is {@link SurrogatePrincipalResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
public class SurrogatePrincipalResolverTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void verifyResolverDefault() {
        final PrincipalResolver resolver = new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository());
        final UsernamePasswordCredential credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final Principal p = resolver.resolve(credential);
        assertNotNull(p);
        assertEquals(p.getId(), credential.getId());
    }

    @Test
    public void verifyResolverAttribute() {
        final PrincipalResolver resolver = new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository(), "cn");
        final UsernamePasswordCredential credential = CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword();
        final Principal p = resolver.resolve(credential);
        assertNotNull(p);
        assertTrue(p.getId().equals("TEST"));
    }

    @Test
    public void verifyResolverSurrogateWithoutPrincipal() {
        final PrincipalResolver resolver = new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository(), "cn");
        final SurrogateUsernamePasswordCredential credential = new SurrogateUsernamePasswordCredential();
        thrown.expect(IllegalArgumentException.class);
        resolver.resolve(credential);
    }
    
    @Test
    public void verifyResolverSurrogate() {
        final PrincipalResolver resolver = new SurrogatePrincipalResolver(CoreAuthenticationTestUtils.getAttributeRepository());
        final SurrogateUsernamePasswordCredential credential = new SurrogateUsernamePasswordCredential();
        credential.setSurrogateUsername("surrogate");
        credential.setUsername("username");
        final Principal p = resolver.resolve(credential, CoreAuthenticationTestUtils.getPrincipal("casuser"), 
                new SimpleTestUsernamePasswordAuthenticationHandler());
        assertNotNull(p);
        assertTrue(p.getId().equals("casuser"));
    }
}
