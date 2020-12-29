package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;

import lombok.ToString;

import java.security.cert.X509Certificate;

/**
 * Returns a principal based on the Subject DNs name.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@ToString(callSuper = true)
public class X509SubjectDNPrincipalResolver extends AbstractX509PrincipalResolver {

    private final String subjectDnFormat;

    public X509SubjectDNPrincipalResolver(final PrincipalResolutionContext context,
                                          final String subjectDnFormat) {
        super(context);
        this.subjectDnFormat = subjectDnFormat;
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        return subjectDnFormat == null
            ? certificate.getSubjectDN().getName()
            : certificate.getSubjectX500Principal().getName(subjectDnFormat);
    }
}
