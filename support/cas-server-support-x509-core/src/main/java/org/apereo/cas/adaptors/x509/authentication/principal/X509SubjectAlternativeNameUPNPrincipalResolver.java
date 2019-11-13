package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.principal.PrincipalFactory;

import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Set;

/**
 * Extracts Subject Alternative Name UPN extension from the provided certificate if available as a resolved principal id.
 *
 * @author Dmitriy Kopylenko
 * @author Hal Deadman
 * @since 4.1.0
 */
@Slf4j
@ToString(callSuper = true)
@NoArgsConstructor
public class X509SubjectAlternativeNameUPNPrincipalResolver extends AbstractX509PrincipalResolver {

    public X509SubjectAlternativeNameUPNPrincipalResolver(final IPersonAttributeDao attributeRepository,
                                                          final PrincipalFactory principalFactory, final boolean returnNullIfNoAttributes,
                                                          final String principalAttributeName,
                                                          final String alternatePrincipalAttribute,
                                                          final boolean useCurrentPrincipalId,
                                                          final boolean resolveAttributes,
                                                          final Set<String> activeAttributeRepositoryIdentifiers) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes,
            principalAttributeName, alternatePrincipalAttribute, useCurrentPrincipalId,
            resolveAttributes, activeAttributeRepositoryIdentifiers);
    }

    @Override
    protected String resolvePrincipalInternal(final X509Certificate certificate) {
        LOGGER.debug("Resolving principal from Subject Alternative Name UPN for [{}]", certificate);
        try {
            val upnString = X509UPNExtractorUtils.extractUPNString(certificate);
            return StringUtils.isNotBlank(upnString) ? upnString : getAlternatePrincipal(certificate);
        } catch (final CertificateParsingException e) {
            LOGGER.error("Error is encountered while trying to retrieve subject alternative names collection from certificate", e);
            return getAlternatePrincipal(certificate);
        }
    }
}
