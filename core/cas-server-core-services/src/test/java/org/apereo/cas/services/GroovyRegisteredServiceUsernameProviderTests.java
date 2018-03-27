package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link GroovyRegisteredServiceUsernameProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class GroovyRegisteredServiceUsernameProviderTests {

    @Test
    public void verifyUsernameProvider() {
        final GroovyRegisteredServiceUsernameProvider p = new GroovyRegisteredServiceUsernameProvider();
        p.setGroovyScript("file:src/test/resources/uid.groovy");
        final String id =
            p.resolveUsername(RegisteredServiceTestUtils.getPrincipal(), RegisteredServiceTestUtils.getService(),
                RegisteredServiceTestUtils.getRegisteredService());
        assertEquals("test", id);
    }
}
