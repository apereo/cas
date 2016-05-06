package org.apereo.cas.support.spnego.authentication.principal;

import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.Principal;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;


/**
 * @author Misagh Moayyed
 * @since 3.0.0
 */
public class SpnegoCredentialsTests {

    @Test
    public void verifyToStringWithNoPrincipal() {
        final SpnegoCredential credentials = new SpnegoCredential(new byte[] {});

        assertTrue(credentials.toString().contains("unknown"));
    }

    @Test
    public void verifyToStringWithPrincipal() {
        final SpnegoCredential credentials = new SpnegoCredential(new byte[] {});
        final Principal principal = new DefaultPrincipalFactory().createPrincipal("test");
        credentials.setPrincipal(principal);
        assertEquals("test", credentials.toString());
    }

    /**
     * Important for SPNEGO in particular as the credential will be hashed prior to Principal resolution
     */
    @Test
    public void verifyCredentialsHashSafelyWithoutPrincipal() {
        final SpnegoCredential credential = new SpnegoCredential(new byte[] {});
        final Set<SpnegoCredential> set = new HashSet<>();
        try {
            set.add(credential);
        } catch(final Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Make sure that when the Principal becomes populated / changes we return a new hash
     */
    @Test
    public void verifyPrincipalAffectsHash(){
        final SpnegoCredential credential = new SpnegoCredential(new byte[] {});
        final int hash1 = credential.hashCode();
        final Principal principal = new DefaultPrincipalFactory().createPrincipal("test");
        credential.setPrincipal(principal);
        final int hash2 = credential.hashCode();
        assertNotEquals(hash1, hash2);
    }
}
