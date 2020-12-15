package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.util.LoggingUtils;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.security.cert.CertificateParsingException;
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

    public X509SubjectAlternativeNameUPNPrincipalResolver(final PrincipalResolutionContext context,
                                                          final String alternatePrincipalAttribute) {
        super(context, alternatePrincipalAttribute);
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        LOGGER.debug("Resolving principal from Subject Alternative Name UPN for [{}]", certificate);
        try {
            val upnString = X509UPNExtractorUtils.extractUPNString(certificate);
            return StringUtils.isNotBlank(upnString) ? upnString : getAlternatePrincipal(certificate);
        } catch (final CertificateParsingException e) {
            LoggingUtils.error(LOGGER, e);
            return getAlternatePrincipal(certificate);
        }
    }
}
