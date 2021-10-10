package org.apereo.cas.adaptors.radius;

import org.apereo.cas.adaptors.radius.server.NonBlockingRadiusServer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * This is {@link NonBlockingRadiusServerTests}.
 * Runs test cases against a radius server running on "https://console.ironwifi.com/".
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Radius")
@EnabledOnOs(OS.LINUX)
public class NonBlockingRadiusServerTests extends AbstractRadiusServerTests {
    @Override
    public RadiusServer getRadiusServer() {
        val factory = RadiusClientFactory.builder()
            .authenticationPort(ACCOUNTING_PORT)
            .authenticationPort(AUTHENTICATION_PORT)
            .socketTimeout(1)
            .inetAddress(INET_ADDRESS)
            .sharedSecret(SECRET)
            .build();
        return new NonBlockingRadiusServer(RadiusProtocol.MSCHAPv2, factory);
    }
}
