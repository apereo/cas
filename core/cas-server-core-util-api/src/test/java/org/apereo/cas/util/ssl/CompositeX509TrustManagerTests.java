package org.apereo.cas.util.ssl;

import org.apereo.cas.util.crypto.CertUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CompositeX509TrustManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("X509")
public class CompositeX509TrustManagerTests {
    @Test
    public void verifyIssuers() throws Exception {
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);

        val managers = Arrays.stream(tmf.getTrustManagers())
            .filter(tm -> tm instanceof X509TrustManager)
            .map(X509TrustManager.class::cast)
            .collect(Collectors.toList());

        val mgr = new CompositeX509TrustManager(managers);
        assertNotNull(mgr.getAcceptedIssuers());
    }

    @Test
    public void verifyCert() throws Exception {
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);

        val managers = Arrays.stream(tmf.getTrustManagers())
            .filter(tm -> tm instanceof X509TrustManager)
            .map(X509TrustManager.class::cast)
            .collect(Collectors.toList());

        val mgr = new CompositeX509TrustManager(managers);
        val certs = new X509Certificate[]{CertUtils.readCertificate(new ClassPathResource("x509.crt").getInputStream())};
        assertThrows(CertificateException.class, () -> mgr.checkClientTrusted(certs, "RSA"));
        assertThrows(CertificateException.class, () -> mgr.checkServerTrusted(certs, "RSA"));
    }
}
