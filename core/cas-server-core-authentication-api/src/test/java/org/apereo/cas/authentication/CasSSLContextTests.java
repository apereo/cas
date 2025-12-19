package org.apereo.cas.authentication;

import module java.base;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSSLContextTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Authentication")
class CasSSLContextTests {
    private static String contactUrl(final String addr, final CasSSLContext context) throws Exception {
        val url = new URI(addr).toURL();
        val connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        connection.setSSLSocketFactory(context.getSslContext().getSocketFactory());
        try (val is = connection.getInputStream()) {
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }

    @Test
    void verifyOperationBySystem() {
        val system = CasSSLContext.system();
        assertNotNull(system.getSslContext());
        assertNotNull(system.getKeyManagers());
        assertNotNull(system.getTrustManagers());
        assertNotNull(system.getHostnameVerifier());
        assertNotNull(system.getTrustManagerFactory());
        assertNotNull(system.getKeyManagerFactory());
        assertNotNull(system.getKeyManagers());
        assertThrows(Exception.class, () -> contactUrl("https://expired.badssl.com/", system));
        assertThrows(Exception.class, () -> contactUrl("https://self-signed.badssl.com/", system));
    }

    @Test
    void verifyOperationDisabled() throws Throwable {
        val disabled = CasSSLContext.disabled();
        assertNotNull(disabled.getSslContext());
        assertNotNull(disabled.getKeyManagers());
        assertNotNull(disabled.getTrustManagers());
        assertNotNull(disabled.getHostnameVerifier());
        assertNotNull(disabled.getTrustManagerFactory());
        assertNotNull(disabled.getKeyManagerFactory());
        assertNotNull(contactUrl("https://expired.badssl.com/", disabled));
        assertNotNull(contactUrl("https://self-signed.badssl.com/", disabled));
        assertNotNull(contactUrl("https://untrusted-root.badssl.com/", disabled));
    }
}
