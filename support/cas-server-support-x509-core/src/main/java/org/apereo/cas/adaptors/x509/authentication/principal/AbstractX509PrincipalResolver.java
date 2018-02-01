package org.apereo.cas.adaptors.x509.authentication.principal;

import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.services.persondir.IPersonAttributeDao;
import java.security.cert.X509Certificate;

/**
 * Abstract class in support of multiple resolvers for X509 Certificates.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
@ToString(callSuper = true)
@NoArgsConstructor
public abstract class AbstractX509PrincipalResolver extends PersonDirectoryPrincipalResolver {

    public AbstractX509PrincipalResolver(final IPersonAttributeDao attributeRepository,
                                         final PrincipalFactory principalFactory, final boolean returnNullIfNoAttributes,
                                         final String principalAttributeName) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes, principalAttributeName);
    }

    @Override
    protected String extractPrincipalId(final Credential credential, final Principal currentPrincipal) {
        return resolvePrincipalInternal(((X509CertificateCredential) credential).getCertificate());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof X509CertificateCredential;
    }

    /**
     * Resolve principal internally, and return the id.
     *
     * @param certificate the certificate
     * @return the string
     */
    protected abstract String resolvePrincipalInternal(X509Certificate certificate);
}
