package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.principal.PrincipalFactory;

import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Set;


/**
 * Credential to principal resolver that extracts Subject Alternative Name RFC822 extension
 * from the provided certificate if available as a resolved principal id.
 *
 * @author Hal Deadman
 * @since 6.1.0
 */
@Slf4j
@ToString(callSuper = true)
@NoArgsConstructor
public class X509SubjectAlternativeNameRFC822EmailPrincipalResolver extends AbstractX509PrincipalResolver {

    public X509SubjectAlternativeNameRFC822EmailPrincipalResolver(final IPersonAttributeDao attributeRepository,
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
        LOGGER.debug("Resolving principal from Subject Alternative Name RFC8222 type (email) for [{}]", certificate);
        try {
            val subjectAltNames = certificate.getSubjectAlternativeNames();
            val email = getRFC822EmailAddress(subjectAltNames);
            if (email != null) {
                return email;
            }
        } catch (final CertificateParsingException e) {
            LOGGER.error("Error encountered while trying to retrieve subject alternative names collection from certificate [{}]", e.getMessage(), e);
        }
        return getAlternatePrincipal(certificate);
    }

}
