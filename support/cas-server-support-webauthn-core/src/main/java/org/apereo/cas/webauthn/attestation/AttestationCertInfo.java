package org.apereo.cas.webauthn.attestation;

import com.yubico.internal.util.CertificateParser;
import com.yubico.webauthn.data.ByteArray;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * This is {@link AttestationCertInfo}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Getter
public class AttestationCertInfo {
    private final ByteArray der;
    private final String text;

    public AttestationCertInfo(final ByteArray certDer) {
        der = certDer;
        X509Certificate cert = null;
        try {
            cert = CertificateParser.parseDer(certDer.getBytes());
        } catch (final CertificateException e) {
            LOGGER.error("Failed to parse attestation certificate", e);
        }
        if (cert == null) {
            text = null;
        } else {
            text = cert.toString();
        }
    }
}
