package org.apereo.cas.util.crypto;

import org.apereo.cas.util.DateTimeUtils;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cryptacular.util.CertUtil;
import org.springframework.core.io.InputStreamSource;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;


/**
 * Utility class with methods to support various operations on X.509 certs.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 *
 */
public final class CertUtils {

    /** X509 certificate type. */
    public static final String X509_CERTIFICATE_TYPE = "X509";


    /** Suppressed constructor of utility class. */
    private CertUtils() {
    }

    /**
     * Determines whether the given CRL is expired by examining the nextUpdate field.
     *
     * @param crl CRL to examine.
     *
     * @return True if current system time is after CRL next update, false otherwise.
     */
    public static boolean isExpired(final X509CRL crl) {
        return isExpired(crl, ZonedDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Determines whether the given CRL is expired by comparing the nextUpdate field
     * with a given date.
     *
     * @param crl CRL to examine.
     * @param reference Reference date for comparison.
     *
     * @return True if reference date is after CRL next update, false otherwise.
     */
    public static boolean isExpired(final X509CRL crl, final ZonedDateTime reference) {
        return reference.isAfter(DateTimeUtils.zonedDateTimeOf(crl.getNextUpdate()));
    }

    /**
     * Read certificate.
     *
     * @param resource the resource to read the cert from
     * @return the x 509 certificate
     */
    public static X509Certificate readCertificate(final InputStreamSource resource) {
        try (InputStream in = resource.getInputStream()) {
            return CertUtil.readCertificate(in);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Error reading certificate " + resource, e);
        }
    }

    /**
     * Creates a unique and human-readable representation of the given certificate.
     *
     * @param cert Certificate.
     *
     * @return String representation of a certificate that includes the subject and serial number.
     */
    public static String toString(final X509Certificate cert) {
        return new ToStringBuilder(cert, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("subjectDn", cert.getSubjectDN())
                .append("serialNumber", cert.getSerialNumber())
                .build();
    }

    /**
     * Gets a certificate factory for creating X.509 artifacts.
     *
     * @return X509 certificate factory.
     */
    public static CertificateFactory getCertificateFactory() {
        try {
            return CertificateFactory.getInstance(X509_CERTIFICATE_TYPE);
        } catch (final CertificateException e) {
            throw new IllegalStateException("X509 certificate type not supported by default provider.", e);
        }
    }
}
