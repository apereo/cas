package org.apereo.cas.web.extractcert;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;

/**
 * 
 * Interface to get an X509 certificate from {@link HttpServletRequest}. e.g.
 * from pem encoded cert on HTTP header.
 * 
 * @author Hal Deadman
 * @since 5.3.0
 * 
 */
@FunctionalInterface
public interface X509CertificateExtractor {

    /**
     * @param request HttpServletRequest that may contain X509 certificate
     * @return X509Certificate array where the first element is user's client
     *         certificate
     */
    X509Certificate[] extract(HttpServletRequest request);

}
