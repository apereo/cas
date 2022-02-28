package org.apereo.cas.acme;

import java.util.Collection;

/**
 * This is {@link AcmeCertificateManager}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface AcmeCertificateManager {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "acmeCertificateManager";

    /**
     * Generates a certificate for the given domains. Also takes care for the registration
     * process.
     *
     * @param domains Domains to get a common certificate for
     * @throws Exception the exception
     */
    void fetchCertificate(Collection<String> domains) throws Exception;
}
