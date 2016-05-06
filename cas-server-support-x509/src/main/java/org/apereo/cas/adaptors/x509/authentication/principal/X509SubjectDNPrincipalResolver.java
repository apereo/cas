package org.apereo.cas.adaptors.x509.authentication.principal;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;

/**
 * Returns a principal based on the Subject DNs name.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@RefreshScope
@Component("x509SubjectDNPrincipalResolver")
public class X509SubjectDNPrincipalResolver extends AbstractX509PrincipalResolver {

    @Override
    protected String resolvePrincipalInternal(
            final X509Certificate certificate) {
        return certificate.getSubjectDN().getName();
    }
}
