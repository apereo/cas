package org.apereo.cas.adaptors.radius;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import net.jradius.dictionary.vsa_microsoft.Attr_MSCHAP2Success;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AbstractRadiusServerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Radius")
@EnabledIfPortOpen(port = 1812)
public abstract class AbstractRadiusServerTests {
    public static final int ACCOUNTING_PORT = 1813;

    public static final int AUTHENTICATION_PORT = 1812;

    public static final String INET_ADDRESS = "localhost";

    public static final String SECRET = "testing123";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void verifyAuthenticationSuccess() throws Exception {
        val server = getRadiusServer();
        val response = server.authenticate("casuser", "Mellon");
        assertEquals(2, response.getCode());
        assertFalse(response.getAttributes().isEmpty());
        assertTrue(response.getAttributes().stream().anyMatch(a -> a.getAttributeName().equals(Attr_MSCHAP2Success.NAME)));
    }

    public abstract RadiusServer getRadiusServer();

    @Test
    public void verifyAuthenticationFails() throws Exception {
        val server = getRadiusServer();
        val response = server.authenticate("casuser", "badpsw");
        assertNull(response);
    }
}
