package org.apereo.cas.adaptors.radius;

import lombok.val;
import net.jradius.dictionary.vsa_microsoft.Attr_MSCHAP2Success;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import java.security.Security;

import static org.junit.Assert.*;

/**
 * This is {@link AbstractRadiusServerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public abstract class AbstractRadiusServerTests {
    public static final int ACCOUNTING_PORT = 6940;
    public static final int AUTHENTICATION_PORT = 6939;
    public static final String INET_ADDRESS = "130.211.138.166";
    public static final String SECRET = "3SJRWyo1pOBa47M";

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
