package org.apereo.cas.adaptors.radius;

import org.apereo.cas.adaptors.radius.server.BlockingRadiusServer;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import net.jradius.exception.TimeoutException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BlockingRadiusServerTests}.
 * Runs test cases against a radius server running on "https://console.ironwifi.com/".
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Radius")
@EnabledIfPortOpen(port = 1812)
public class BlockingRadiusServerTests extends AbstractRadiusServerTests {

    public static final String XYZ = "xyz";

    @Test
    public void verifyBadSecret() {
        assertThrows(TimeoutException.class,
            () -> new BlockingRadiusServer(RadiusProtocol.MSCHAPv2,
                new RadiusClientFactory(ACCOUNTING_PORT, AUTHENTICATION_PORT, 1, INET_ADDRESS, XYZ))
                .authenticate(XYZ, XYZ));
    }

    @Test
    public void verifyBadPorts() {
        assertThrows(TimeoutException.class,
            () -> new BlockingRadiusServer(RadiusProtocol.MSCHAPv2, new RadiusClientFactory(1234, 4567, 1, INET_ADDRESS, XYZ))
            .authenticate(XYZ, XYZ));
    }

    @Test
    public void verifyBadAddress() {
        assertThrows(TimeoutException.class,
            () -> new BlockingRadiusServer(RadiusProtocol.MSCHAPv2, new RadiusClientFactory(1234, 4567, 1, "131.211.138.166", "1234"))
                .authenticate(XYZ, XYZ));
    }

    @Override
    public RadiusServer getRadiusServer() {
        return new BlockingRadiusServer(RadiusProtocol.MSCHAPv2,
            new RadiusClientFactory(ACCOUNTING_PORT, AUTHENTICATION_PORT, 1, INET_ADDRESS, SECRET));
    }
}
