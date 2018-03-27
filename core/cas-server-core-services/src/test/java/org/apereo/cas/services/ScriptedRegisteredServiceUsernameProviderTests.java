package org.apereo.cas.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link ScriptedRegisteredServiceUsernameProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class ScriptedRegisteredServiceUsernameProviderTests {
    @Test
    public void verifyUsernameProvider() {
        final ScriptedRegisteredServiceUsernameProvider p = new ScriptedRegisteredServiceUsernameProvider();
        p.setScript("file:src/test/resources/uidscript.groovy");
        final String id =
                p.resolveUsername(RegisteredServiceTestUtils.getPrincipal(), RegisteredServiceTestUtils.getService(),
                    RegisteredServiceTestUtils.getRegisteredService());
        assertEquals("test", id);
    }
}
