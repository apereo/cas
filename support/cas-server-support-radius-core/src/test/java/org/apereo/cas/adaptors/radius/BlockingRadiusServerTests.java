package org.apereo.cas.adaptors.radius;

import org.apereo.cas.adaptors.radius.server.BlockingRadiusServer;

import lombok.val;
import net.jradius.exception.TimeoutException;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

/**
 * This is {@link BlockingRadiusServerTests}.
 * Runs test cases against a radius server running on "https://console.ironwifi.com/".
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class BlockingRadiusServerTests extends AbstractRadiusServerTests {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void verifyBadSecret() throws Exception {
        thrown.expect(TimeoutException.class);
        val server = new BlockingRadiusServer(RadiusProtocol.MSCHAPv2,
            new RadiusClientFactory(ACCOUNTING_PORT, AUTHENTICATION_PORT, 1,
                INET_ADDRESS, "xyz"));
        server.authenticate("xyz", "xyz");
    }

    @Test
    public void verifyBadPorts() throws Exception {
        thrown.expect(TimeoutException.class);
        val server = new BlockingRadiusServer(RadiusProtocol.MSCHAPv2,
            new RadiusClientFactory(1234, 4567, 1,
                INET_ADDRESS, "xyz"));
        server.authenticate("xyz", "xyz");
    }

    @Test
    public void verifyBadAddress() throws Exception {
        thrown.expect(TimeoutException.class);
        val server = new BlockingRadiusServer(RadiusProtocol.MSCHAPv2,
            new RadiusClientFactory(1234, 4567, 1,
                "131.211.138.166", "1234"));
        server.authenticate("xyz", "xyz");
    }

    @Override
    public RadiusServer getRadiusServer() {
        return new BlockingRadiusServer(RadiusProtocol.MSCHAPv2,
            new RadiusClientFactory(ACCOUNTING_PORT, AUTHENTICATION_PORT, INET_ADDRESS, SECRET));
    }
}
