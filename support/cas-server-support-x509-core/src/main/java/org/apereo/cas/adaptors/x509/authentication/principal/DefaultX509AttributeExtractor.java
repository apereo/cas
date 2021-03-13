package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Get the default set of attributes from an X509 certificate.
 * @author Hal Deadman
 * @since 6.4
 */
public class DefaultX509AttributeExtractor implements X509AttributeExtractor {

    @Override
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
            val subjectAltNames = X509ExtractorUtils.getSubjectAltNames(certificate);
            X509ExtractorUtils.getRFC822EmailAddress(subjectAltNames).ifPresent(
                email -> attributes.put("x509Rfc822Email", CollectionUtils.wrapList(email)));
            X509UPNExtractorUtils.extractUPNString(subjectAltNames).ifPresent(
                upn -> attributes.put("x509subjectUPN", CollectionUtils.wrapList(upn)));
        }
        return attributes;
    }
}
