package org.apereo.cas.support.spnego.authentication.principal;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;


/**
 * @author Misagh Moayyed
 * @since 3.0.0
 */
@Slf4j
public class SpnegoCredentialsTests {

    @Test
    public void verifyToStringWithNoPrincipal() {
        final var credentials = new SpnegoCredential(new byte[] {});
        assertTrue(credentials.getId().contains("unknown"));
    }

    @Test
    public void verifyToStringWithPrincipal() {
        final var credentials = new SpnegoCredential(new byte[] {});
        final var principal = new DefaultPrincipalFactory().createPrincipal("test");
        credentials.setPrincipal(principal);
        assertEquals("test", credentials.getId());
    }

    /**
     * Important for SPNEGO in particular as the credential will be hashed prior to Principal resolution
     */
    @Test
    public void verifyCredentialsHashSafelyWithoutPrincipal() {
        final var credential = new SpnegoCredential(new byte[] {});
        final Set<SpnegoCredential> set = new HashSet<>();
        try {
            set.add(credential);
        } catch(final Exception e) {
            throw new AssertionError(e.getMessage());
        }
    }

    /**
     * Make sure that when the Principal becomes populated / changes we return a new hash
     */
    @Test
    public void verifyPrincipalAffectsHash(){
        final var credential = new SpnegoCredential(new byte[] {});
        final var hash1 = credential.hashCode();
        final var principal = new DefaultPrincipalFactory().createPrincipal("test");
        credential.setPrincipal(principal);
        final var hash2 = credential.hashCode();
        assertNotEquals(hash1, hash2);
    }
}
