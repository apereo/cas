package org.jasig.cas.adaptors.radius;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Unit test for {@link JRadiusServerImpl}.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/test-context.xml")
public class JRadiusServerImplTest {

    @Autowired
    private JRadiusServerImpl radiusServer;

    /**
     * Presently this only tests component wiring.
     * An external RADIUS server test fixture is required for thorough testing.
     */
    @Test
    public void verifyAuthenticate() {
        assertNotNull(this.radiusServer);
    }
}
