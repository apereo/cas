package org.apereo.cas.services;

import lombok.val;
import org.junit.jupiter.api.Test;

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
        val p = new GroovyRegisteredServiceUsernameProvider();
        p.setGroovyScript("file:src/test/resources/uid.groovy");
        val id =
            p.resolveUsername(RegisteredServiceTestUtils.getPrincipal(), RegisteredServiceTestUtils.getService(),
                RegisteredServiceTestUtils.getRegisteredService());
        assertEquals("test", id);
    }
}
