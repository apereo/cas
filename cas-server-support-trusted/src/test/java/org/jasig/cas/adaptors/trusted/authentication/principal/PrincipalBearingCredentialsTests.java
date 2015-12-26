package org.jasig.cas.adaptors.trusted.authentication.principal;

import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class PrincipalBearingCredentialsTests {

    private PrincipalBearingCredential principalBearingCredentials;

    @Before
    public void setUp() throws Exception {
        this.principalBearingCredentials = new PrincipalBearingCredential(new DefaultPrincipalFactory().createPrincipal("test"));
    }

    @Test
    public void verifyGetOfPrincipal() {
        assertEquals("test", this.principalBearingCredentials.getPrincipal().getId());
    }
}
