package org.jasig.cas.adaptors.x509.authentication.principal;

import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;

/**
 * Returns a principal based on the Subject DNs name.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Component("x509SubjectDNPrincipalResolver")
public final class X509SubjectDNPrincipalResolver extends AbstractX509PrincipalResolver {

    @Override
    protected String resolvePrincipalInternal(
            final X509Certificate certificate) {
        return certificate.getSubjectDN().getName();
    }
}
