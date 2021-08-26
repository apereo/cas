package org.apereo.cas.adaptors.radius;

import org.apereo.cas.util.HttpRequestUtils;

import lombok.val;
import net.jradius.dictionary.vsa_microsoft.Attr_MSCHAP2Success;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AbstractRadiusServerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public abstract class AbstractRadiusServerTests {
    public static final int ACCOUNTING_PORT = 1813;

    public static final int AUTHENTICATION_PORT = 1812;

    public static final String INET_ADDRESS = "localhost";

    public static final String SECRET = "testing123";

    static {
        Security.addProvider(new BouncyCastleProvider());
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        request.setLocalAddr("4.5.6.7");
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
    }

    @Test
    public void verifyAuthenticationSuccess() throws Exception {
        val server = getRadiusServer();
        val response = server.authenticate("casuser", "Mellon");
        assertEquals(2, response.getCode());
        assertFalse(response.getAttributes().isEmpty());
        assertTrue(response.getAttributes().stream()
            .anyMatch(a -> a.getAttributeName().equals(Attr_MSCHAP2Success.NAME)));
    }

    public abstract RadiusServer getRadiusServer();

    @Test
    public void verifyAuthenticationFails() throws Exception {
        val server = getRadiusServer();
        val response = server.authenticate("casuser", "badpsw");
        assertNull(response);
    }
}
