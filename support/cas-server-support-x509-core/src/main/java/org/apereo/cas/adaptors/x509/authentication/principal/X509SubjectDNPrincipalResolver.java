package org.apereo.cas.adaptors.x509.authentication.principal;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.services.persondir.IPersonAttributeDao;
import java.security.cert.X509Certificate;
import lombok.NoArgsConstructor;

/**
 * Returns a principal based on the Subject DNs name.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
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
