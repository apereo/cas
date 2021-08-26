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
        val factory = RadiusClientFactory.builder()
            .authenticationPort(ACCOUNTING_PORT)
            .authenticationPort(AUTHENTICATION_PORT)
            .socketTimeout(1)
            .inetAddress(INET_ADDRESS)
            .sharedSecret(USERNAME)
            .build();

        assertThrows(TimeoutException.class,
            () -> new BlockingRadiusServer(RadiusProtocol.MSCHAPv2, factory).authenticate(USERNAME, USERNAME));
    }

    @Test
    public void verifyBadPorts() {
        val factory = RadiusClientFactory.builder()
            .authenticationPort(1234)
            .authenticationPort(5678)
            .socketTimeout(1)
            .inetAddress(INET_ADDRESS)
            .sharedSecret(USERNAME)
            .build();
        assertThrows(TimeoutException.class,
            () -> new BlockingRadiusServer(RadiusProtocol.MSCHAPv2, factory).authenticate(USERNAME, USERNAME));
    }

    @Test
    public void verifyBadAddress() {
        val factory = RadiusClientFactory.builder()
            .authenticationPort(1234)
            .authenticationPort(5678)
            .socketTimeout(1)
            .inetAddress("131.211.138.166")
            .sharedSecret("1234")
            .build();
        assertThrows(TimeoutException.class,
            () -> new BlockingRadiusServer(RadiusProtocol.MSCHAPv2, factory).authenticate(USERNAME, USERNAME));
    }

    @Test
    public void verifyNasSettings() {
        val factory = RadiusClientFactory.builder()
            .authenticationPort(1234)
            .authenticationPort(5678)
            .socketTimeout(1)
            .inetAddress("131.211.138.166")
            .sharedSecret("1234")
            .build();
        val context = RadiusServerConfigurationContext.builder()
            .protocol(RadiusProtocol.MSCHAPv2)
            .radiusClientFactory(factory)
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
        val factory = RadiusClientFactory.builder()
            .authenticationPort(ACCOUNTING_PORT)
            .authenticationPort(AUTHENTICATION_PORT)
            .socketTimeout(1)
            .inetAddress(INET_ADDRESS)
            .sharedSecret(SECRET)
            .build();
        return new BlockingRadiusServer(RadiusProtocol.MSCHAPv2, factory);
    }
}
