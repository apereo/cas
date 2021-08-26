package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.security.cert.X509Certificate;

/**
 * Extracts Subject Alternative Name UPN extension from the provided certificate if available as a resolved principal id.
 *
 * @author Dmitriy Kopylenko
 * @author Hal Deadman
 * @since 4.1.0
 */
@Slf4j
@ToString(callSuper = true)
public class X509SubjectAlternativeNameUPNPrincipalResolver extends AbstractX509PrincipalResolver {

    public X509SubjectAlternativeNameUPNPrincipalResolver(final PrincipalResolutionContext context) {
        super(context);
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        LOGGER.debug("Resolving principal from Subject Alternative Name UPN for [{}]", certificate);
        val subjectAltNames = X509ExtractorUtils.getSubjectAltNames(certificate);
        val upnString = X509UPNExtractorUtils.extractUPNString(subjectAltNames);
        return upnString.orElse(getAlternatePrincipal(certificate));
    }
}
