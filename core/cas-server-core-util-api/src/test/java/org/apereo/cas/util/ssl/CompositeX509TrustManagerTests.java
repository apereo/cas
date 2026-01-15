package org.apereo.cas.util.ssl;

import module java.base;
import org.apereo.cas.util.crypto.CertUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CompositeX509TrustManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("X509")
class CompositeX509TrustManagerTests {
    @Test
    void verifyIssuers() throws Throwable {
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);

        val managers = Arrays.stream(tmf.getTrustManagers())
            .filter(X509TrustManager.class::isInstance)
            .map(X509TrustManager.class::cast)
            .collect(Collectors.toList());

        val mgr = new CompositeX509TrustManager(managers);
        assertNotNull(mgr.getAcceptedIssuers());
    }

    @Test
    void verifyCert() throws Throwable {
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);

        val managers = Arrays.stream(tmf.getTrustManagers())
            .filter(X509TrustManager.class::isInstance)
            .map(X509TrustManager.class::cast)
            .collect(Collectors.toList());

        val mgr = new CompositeX509TrustManager(managers);
        val certs = new X509Certificate[]{CertUtils.readCertificate(new ClassPathResource("x509.crt").getInputStream())};
        assertThrows(CertificateException.class, () -> mgr.checkClientTrusted(certs, "RSA"));
        assertThrows(CertificateException.class, () -> mgr.checkServerTrusted(certs, "RSA"));
    }
}
