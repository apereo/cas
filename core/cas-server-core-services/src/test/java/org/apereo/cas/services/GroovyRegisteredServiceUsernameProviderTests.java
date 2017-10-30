package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link GroovyRegisteredServiceUsernameProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class GroovyRegisteredServiceUsernameProviderTests {

    @Test
    public void verifyUsernameProvider() {
        final GroovyRegisteredServiceUsernameProvider p = new GroovyRegisteredServiceUsernameProvider();
        p.setGroovyScript("file:src/test/resources/uid.groovy");
        final String id =
                p.resolveUsername(CoreAuthenticationTestUtils.getPrincipal(), CoreAuthenticationTestUtils.getService(),
                CoreAuthenticationTestUtils.getRegisteredService());
        assertEquals(id, "test");
    }
}
