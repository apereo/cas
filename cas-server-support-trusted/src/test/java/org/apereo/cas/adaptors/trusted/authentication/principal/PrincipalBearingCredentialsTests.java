package org.apereo.cas.adaptors.trusted.authentication.principal;

import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Assert.assertEquals("test", this.principalBearingCredentials.getPrincipal().getId());
    }
}
