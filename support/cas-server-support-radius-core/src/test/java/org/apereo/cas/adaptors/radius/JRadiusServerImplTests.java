package org.apereo.cas.adaptors.radius;

import net.jradius.dictionary.vsa_microsoft.Attr_MSCHAP2Success;
import net.jradius.exception.TimeoutException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.security.Security;

import static org.junit.Assert.*;

/**
 * This is {@link JRadiusServerImplTests}.
 * Runs test cases against a radius server running on "https://console.ironwifi.com/".
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JRadiusServerImplTests {
    private static final int ACCOUNTING_PORT = 6940;
    private static final int AUTHENTICATION_PORT = 6939;
    private static final String INET_ADDRESS = "130.211.138.166";
    private static final String SECRET = "3SJRWyo1pOBa47M";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void verifyAuthenticationSuccess() throws Exception {
        final JRadiusServerImpl server = new JRadiusServerImpl(RadiusProtocol.MSCHAPv2,
                new RadiusClientFactory(ACCOUNTING_PORT, AUTHENTICATION_PORT, INET_ADDRESS, SECRET));
        final RadiusResponse response = server.authenticate("casuser", "Mellon");
        assertEquals(response.getCode(), 2);
        assertFalse(response.getAttributes().isEmpty());
        assertTrue(response.getAttributes().stream().anyMatch(a -> a.getAttributeName().equals(Attr_MSCHAP2Success.NAME)));
    }

    @Test
    public void verifyAuthenticationFails() throws Exception {
        final JRadiusServerImpl server = new JRadiusServerImpl(RadiusProtocol.MSCHAPv2,
                new RadiusClientFactory(ACCOUNTING_PORT, AUTHENTICATION_PORT, INET_ADDRESS, SECRET));
        final RadiusResponse response = server.authenticate("casuser", "badpsw");
        assertNull(response);
    }

    @Test
    public void verifyBadSecret() throws Exception {
        thrown.expect(TimeoutException.class);
        final JRadiusServerImpl server = new JRadiusServerImpl(RadiusProtocol.MSCHAPv2,
                new RadiusClientFactory(ACCOUNTING_PORT, AUTHENTICATION_PORT, 1,
                        INET_ADDRESS, "xyz"));
        server.authenticate("xyz", "xyz");
    }

    @Test
    public void verifyBadPorts() throws Exception {
        thrown.expect(TimeoutException.class);
        final JRadiusServerImpl server = new JRadiusServerImpl(RadiusProtocol.MSCHAPv2,
                new RadiusClientFactory(1234, 4567, 1,
                        INET_ADDRESS, "xyz"));
        server.authenticate("xyz", "xyz");
    }

    @Test
    public void verifyBadAddress() throws Exception {
        thrown.expect(TimeoutException.class);
        final JRadiusServerImpl server = new JRadiusServerImpl(RadiusProtocol.MSCHAPv2,
                new RadiusClientFactory(1234, 4567, 1,
                        "131.211.138.166", "1234"));
        server.authenticate("xyz", "xyz");
    }
}
