package org.apereo.cas.adaptors.radius;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Unit test for {@link JRadiusServerImpl}.
 *
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration("/test-context.xml")
public class JRadiusServerImplTests {

    @Autowired
    private RadiusServer radiusServer;

    /**
     * Presently this only tests component wiring.
     * An external RADIUS server test fixture is required for thorough testing.
     */
    @Test
    public void verifyAuthenticate() {
        assertNotNull(this.radiusServer);
    }
}
