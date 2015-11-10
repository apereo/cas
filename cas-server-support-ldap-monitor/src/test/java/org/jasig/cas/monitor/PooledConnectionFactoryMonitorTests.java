package org.jasig.cas.monitor;

import org.jasig.cas.adaptors.ldap.AbstractLdapTests;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Unit test for {@link PooledConnectionFactoryMonitor} class.
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/ldap-context.xml", "/ldap-poolmonitor-test.xml"})
public class PooledConnectionFactoryMonitorTests extends AbstractLdapTests {

    @Autowired
    @Qualifier("pooledLdapConnectionFactoryMonitor")
    private PooledConnectionFactoryMonitor monitor;

    @BeforeClass
    public static void bootstrap() throws Exception {
        initDirectoryServer();
    }

    @Test
    public void verifyObserve() throws Exception {
        assertEquals(StatusCode.OK, monitor.observe().getCode());
    }
}
