package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.util.LoggingUtils;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;


/**
 * Credential to principal resolver that extracts Subject Alternative Name RFC822 extension
 * from the provided certificate if available as a resolved principal id.
 *
 * @author Hal Deadman
 * @since 6.1.0
 */
@Slf4j
@ToString(callSuper = true)
public class X509SubjectAlternativeNameRFC822EmailPrincipalResolver extends AbstractX509PrincipalResolver {

    public X509SubjectAlternativeNameRFC822EmailPrincipalResolver(final PrincipalResolutionContext context) {
        super(context);
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        LOGGER.debug("Resolving principal from Subject Alternative Name RFC8222 type (email) for [{}]", certificate);
        try {
            val subjectAltNames = certificate.getSubjectAlternativeNames();
            val email = X509ExtractorUtils.getRFC822EmailAddress(subjectAltNames);
            if (email.isPresent()) {
                return email.get();
            }
        } catch (final CertificateParsingException e) {
            LoggingUtils.error(LOGGER, e);
        }
        return getAlternatePrincipal(certificate);
    }

}
