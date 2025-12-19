package org.apereo.cas.adaptors.x509.authentication.principal;

import module java.base;
import java.security.cert.X509Certificate;

/**
 * Extract person attributes from the certificate.
 * 
 * @author Hal Deadman
 * @since 6.4
 */
@FunctionalInterface
public interface X509AttributeExtractor {

    /**
     * Extract various attributes from the certificate about the person.
     * 
     * @param certificate X509 Certificate
     * @return Map of the attributes
     */
    Map<String, List<Object>> extractPersonAttributes(X509Certificate certificate);

}
