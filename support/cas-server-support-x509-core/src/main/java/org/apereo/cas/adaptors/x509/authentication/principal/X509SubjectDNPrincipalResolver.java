package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.principal.PrincipalFactory;

import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apereo.services.persondir.IPersonAttributeDao;

import java.security.cert.X509Certificate;

/**
 * Returns a principal based on the Subject DNs name.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@ToString(callSuper = true)
@NoArgsConstructor
public class X509SubjectDNPrincipalResolver extends AbstractX509PrincipalResolver {

    public X509SubjectDNPrincipalResolver(final IPersonAttributeDao attributeRepository, final PrincipalFactory principalFactory,
                                          final boolean returnNullIfNoAttributes, final String principalAttributeName) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes, principalAttributeName);
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        return certificate.getSubjectDN().getName();
    }
}
