package org.apereo.cas.adaptors.x509.authentication.principal;

import java.security.cert.X509Certificate;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;

/**
 * Abstract class in support of multiple resolvers for X509 Certificates.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public abstract class AbstractX509PrincipalResolver extends PersonDirectoryPrincipalResolver {

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


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }
}
