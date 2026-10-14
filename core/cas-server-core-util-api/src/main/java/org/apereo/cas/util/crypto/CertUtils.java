package org.apereo.cas.util.crypto;

import module java.base;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cryptacular.util.CertUtil;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


/**
 * Utility class with methods to support various operations on X.509 certs.
 *
 * @author Marvin S. Addison
 * @since 3.4.6
 */
@UtilityClass
public class CertUtils {

    /**
     * X509 certificate type.
     */
    public static final String X509_CERTIFICATE_TYPE = "X509";

    /**
     * Determines whether the given CRL is expired by examining the nextUpdate field.
     *
     * @param crl CRL to examine.
     * @return True if current system time is after CRL next update, false otherwise.
     */
    public static boolean isExpired(final X509CRL crl) {
        return isExpired(crl, ZonedDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Determines whether the given CRL is expired by comparing the nextUpdate field
     * with a given date.
     *
     * @param crl       CRL to examine.
     * @param reference Reference date for comparison.
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
        try (val in = resource.getInputStream()) {
            return CertUtil.readCertificate(in);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Error reading certificate " + resource, e);
        }
    }

    /**
     * Read certificate x 509 certificate.
     *
     * @param resource the resource
     * @return the x 509 certificate
     */
    public static X509Certificate readCertificate(final InputStream resource) {
        return readCertificate(new InputStreamResource(resource));
    }

    /**
     * Creates a unique and human-readable representation of the given certificate.
     *
     * @param cert Certificate.
     * @return String representation of a certificate that includes the subject and serial number.
     */
    public static String toString(final X509Certificate cert) {
        return new ToStringBuilder(cert, ToStringStyle.NO_CLASS_NAME_STYLE)
            .append("subjectDn", cert.getSubjectDN())
            .append("serialNumber", cert.getSerialNumber())
            .build();
    }

    /**
     * Certificate chain without its trust anchor, as sent in an {@code x5c} header: a trailing
     * self-issued certificate is dropped unless it is the only one, since a lone certificate is
     * the signer itself.
     *
     * @param certificateChain the certificate chain, leaf first; may be null
     * @return the chain without the trust anchor, possibly empty
     */
    public static List<X509Certificate> withoutTrustAnchor(final List<X509Certificate> certificateChain) {
        if (certificateChain == null || certificateChain.isEmpty()) {
            return List.of();
        }
        return certificateChain.size() > 1 && isSelfIssued(certificateChain.getLast())
            ? List.copyOf(certificateChain.subList(0, certificateChain.size() - 1))
            : List.copyOf(certificateChain);
    }

    /**
     * Whether the certificate is self-issued, its subject and issuer being the same name, as a trust
     * anchor or a self-signed certificate is.
     *
     * @param certificate the certificate
     * @return true if self-issued
     */
    public static boolean isSelfIssued(final X509Certificate certificate) {
        return certificate.getSubjectX500Principal().equals(certificate.getIssuerX500Principal());
    }

    /**
     * Read trust anchors from PEM resources, each of which may hold several certificates.
     *
     * @param locations the resource locations
     * @return the trust anchors
     */
    public static Set<TrustAnchor> readTrustAnchors(final List<String> locations) {
        return locations
            .stream()
            .map(location -> FunctionUtils.doUnchecked(() -> readCertificates(location)))
            .flatMap(List::stream)
            .map(certificate -> new TrustAnchor(certificate, null))
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Validate a certificate chain, leaf first, as sent in an {@code x5c} header, with PKIX against the trust anchors and
     * without revocation checking. The chain must not carry the trust anchor and no certificate in it may be self-issued,
     * as HAIP 1.0 requires of attestation and credential signers.
     *
     * @param certificateChain the certificate chain, leaf first
     * @param trustAnchors     the trust anchors
     * @return the leaf certificate
     * @throws GeneralSecurityException when the chain is empty, carries a self-issued certificate or does not validate
     */
    public static X509Certificate validateCertificateChain(final List<X509Certificate> certificateChain,
                                                           final Set<TrustAnchor> trustAnchors)
            throws GeneralSecurityException {
        if (certificateChain.isEmpty() || certificateChain.stream().anyMatch(CertUtils::isSelfIssued)) {
            throw new CertificateException("Certificate chain must not be empty or carry a trust anchor or a self-signed certificate");
        }
        val parameters = new PKIXParameters(trustAnchors);
        parameters.setRevocationEnabled(false);
        CertPathValidator.getInstance("PKIX").validate(getCertificateFactory().generateCertPath(certificateChain), parameters);
        return certificateChain.getFirst();
    }

    /**
     * Gets a certificate factory for creating X.509 artifacts.
     *
     * @return X509 certificate factory.
     */
    public static CertificateFactory getCertificateFactory() {
        return FunctionUtils.doUnchecked(() -> CertificateFactory.getInstance(X509_CERTIFICATE_TYPE));
    }

    private static List<X509Certificate> readCertificates(final String location) throws Exception {
        try (val input = ResourceUtils.getResourceFrom(location).getInputStream()) {
            return getCertificateFactory().generateCertificates(input)
                .stream()
                .map(X509Certificate.class::cast)
                .toList();
        }
    }
}
