package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hal Deadman
 * @since 6.4
 */
@Slf4j
public class DefaultX509AttributeExtractor implements X509AttributeExtractor {

    /**
     * Get additional attributes from the certificate.
     *
     * @param certificate X509 Certificate of user
     * @return map of attributes
     */
    public Map<String, List<Object>> extractPersonAttributes(final X509Certificate certificate) {
        val attributes = new LinkedHashMap<String, List<Object>>();

        if (certificate != null) {
            if (StringUtils.isNotBlank(certificate.getSigAlgOID())) {
                attributes.put("sigAlgOid", CollectionUtils.wrapList(certificate.getSigAlgOID()));
            }
            val subjectDn = certificate.getSubjectDN();
            if (subjectDn != null) {
                attributes.put("subjectDn", CollectionUtils.wrapList(subjectDn.getName()));
            }
            val subjectPrincipal = certificate.getSubjectX500Principal();
            if (subjectPrincipal != null) {
                attributes.put("subjectX500Principal", CollectionUtils.wrapList(subjectPrincipal.getName()));
            }
            val issuerDn = certificate.getIssuerDN();
            if (issuerDn != null) {
                attributes.put("issuerDn", CollectionUtils.wrapList(issuerDn.getName()));
            }
            val issuerPrincipal = certificate.getIssuerX500Principal();
            if (issuerPrincipal != null) {
                attributes.put("issuerX500Principal", CollectionUtils.wrapList(issuerPrincipal.getName()));
            }
            try {
                val rfc822Email = X509ExtractorUtils.getRFC822EmailAddress(certificate.getSubjectAlternativeNames());
                if (rfc822Email != null) {
                    attributes.put("x509Rfc822Email", CollectionUtils.wrapList(rfc822Email));
                }
            } catch (final CertificateParsingException e) {
                if (LOGGER.isDebugEnabled()) {
                    LoggingUtils.warn(LOGGER, "Error parsing subject alternative names to get rfc822 email", e);
                }
                LOGGER.warn("Error parsing subject alternative names to get rfc822 email [{}]", e.getMessage());
            }
            try {
                val x509subjectUPN = X509UPNExtractorUtils.extractUPNString(certificate);
                if (x509subjectUPN != null) {
                    attributes.put("x509subjectUPN", CollectionUtils.wrapList(x509subjectUPN));
                }
            } catch (final CertificateParsingException e) {
                LoggingUtils.warn(LOGGER, e);
            }
        }
        return attributes;
    }


}
