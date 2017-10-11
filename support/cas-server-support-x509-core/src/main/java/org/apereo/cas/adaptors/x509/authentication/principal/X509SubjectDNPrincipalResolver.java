package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.services.persondir.IPersonAttributeDao;

import java.security.cert.X509Certificate;

/**
 * Returns a principal based on the Subject DNs name.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class X509SubjectDNPrincipalResolver extends AbstractX509PrincipalResolver {

    public X509SubjectDNPrincipalResolver() {
    }

    public X509SubjectDNPrincipalResolver(final IPersonAttributeDao attributeRepository, 
                                          final PrincipalFactory principalFactory,
                                          final boolean returnNullIfNoAttributes,
                                          final String principalAttributeName) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes, principalAttributeName);
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        return certificate.getSubjectDN().getName();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }
}
