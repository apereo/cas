package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.principal.PrincipalFactory;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apereo.services.persondir.IPersonAttributeDao;

import java.security.cert.X509Certificate;
import java.util.Set;

/**
 * Returns a principal based on the Subject DNs name.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@ToString(callSuper = true)
@RequiredArgsConstructor
public class X509SubjectDNPrincipalResolver extends AbstractX509PrincipalResolver {

    private final String subjectDnFormat;

    public X509SubjectDNPrincipalResolver(final IPersonAttributeDao attributeRepository, final PrincipalFactory principalFactory,
                                          final boolean returnNullIfNoAttributes, final String principalAttributeName,
                                          final boolean useCurrentPrincipalId, final boolean resolveAttributes,
                                          final Set<String> activeAttributeRepositoryIdentifiers,
                                          final String subjectDnFormat) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes,
            principalAttributeName, useCurrentPrincipalId, resolveAttributes,
            activeAttributeRepositoryIdentifiers);
        this.subjectDnFormat = subjectDnFormat;
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        return subjectDnFormat == null
            ? certificate.getSubjectDN().getName()
            : certificate.getSubjectX500Principal().getName(subjectDnFormat);
    }
}
