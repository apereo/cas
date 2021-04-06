package org.apereo.cas.adaptors.radius;

import org.apereo.cas.adaptors.radius.server.BlockingRadiusServer;
import org.apereo.cas.adaptors.radius.server.RadiusServerConfigurationContext;

import lombok.val;
import net.jradius.exception.TimeoutException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BlockingRadiusServerTests}.
 * Runs test cases against a radius server running on "https://console.ironwifi.com/".
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Radius")
@EnabledOnOs(OS.LINUX)
public class BlockingRadiusServerTests extends AbstractRadiusServerTests {

    public static final String USERNAME = UUID.randomUUID().toString();

    @Test
    public void verifyBadSecret() {
        assertThrows(TimeoutException.class,
            () -> new BlockingRadiusServer(RadiusProtocol.MSCHAPv2,
                new RadiusClientFactory(ACCOUNTING_PORT, AUTHENTICATION_PORT, 1, INET_ADDRESS, USERNAME))
                .authenticate(USERNAME, USERNAME));
    }

    @Test
    public void verifyBadPorts() {
        assertThrows(TimeoutException.class,
            () -> new BlockingRadiusServer(RadiusProtocol.MSCHAPv2, new RadiusClientFactory(1234, 4567, 1, INET_ADDRESS, USERNAME))
                .authenticate(USERNAME, USERNAME));
    }

    @Test
    public void verifyBadAddress() {
        assertThrows(TimeoutException.class,
            () -> new BlockingRadiusServer(RadiusProtocol.MSCHAPv2, new RadiusClientFactory(1234, 4567, 1, "131.211.138.166", "1234"))
                .authenticate(USERNAME, USERNAME));
    }

    @Test
    public void verifyNasSettings() {
        val context = RadiusServerConfigurationContext.builder()
            .protocol(RadiusProtocol.MSCHAPv2)
            .radiusClientFactory(new RadiusClientFactory(1234, 4567, 1, "131.211.138.166", "1234"))
            .nasIpAddress("3.2.1.0")
            .nasIpv6Address("0:0:0:0:0:ffff:302:100")
            .nasPort(ACCOUNTING_PORT)
            .nasPortId(1234)
            .nasIdentifier(UUID.randomUUID().toString())
            .nasRealPort(ACCOUNTING_PORT)
            .nasPortType(1)
            .build();
        assertThrows(TimeoutException.class, () -> new BlockingRadiusServer(context).authenticate(USERNAME, USERNAME));
    }

    @Override
    public RadiusServer getRadiusServer() {
        return new BlockingRadiusServer(RadiusProtocol.MSCHAPv2,
            new RadiusClientFactory(ACCOUNTING_PORT, AUTHENTICATION_PORT, 1, INET_ADDRESS, SECRET));
    }
}
