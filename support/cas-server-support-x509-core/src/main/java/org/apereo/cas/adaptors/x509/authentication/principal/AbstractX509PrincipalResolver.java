package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.util.CollectionUtils;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * Abstract class in support of multiple resolvers for X509 Certificates.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@ToString(callSuper = true)
@NoArgsConstructor
@Slf4j
@Setter
public abstract class AbstractX509PrincipalResolver extends PersonDirectoryPrincipalResolver {

    private static int SAN_RFC822_EMAIL_TYPE = 1;

    private String alternatePrincipalAttribute;

    public AbstractX509PrincipalResolver(final IPersonAttributeDao attributeRepository,
                                         final PrincipalFactory principalFactory,
                                         final boolean returnNullIfNoAttributes,
                                         final String principalAttributeName,
                                         final String alternatePrincipalAttribute,
                                         final boolean useCurrentPrincipalId,
                                         final boolean resolveAttributes,
                                         final Set<String> activeAttributeRepositoryIdentifiers) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes,
            principalAttributeName, useCurrentPrincipalId, resolveAttributes,
            activeAttributeRepositoryIdentifiers);
        this.alternatePrincipalAttribute = alternatePrincipalAttribute;
    }

    public AbstractX509PrincipalResolver(final IPersonAttributeDao attributeRepository,
                                         final PrincipalFactory principalFactory,
                                         final boolean returnNullIfNoAttributes,
                                         final String principalAttributeName,
                                         final boolean useCurrentPrincipalId,
                                         final boolean resolveAttributes,
                                         final Set<String> activeAttributeRepositoryIdentifiers) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes,
            principalAttributeName, useCurrentPrincipalId, resolveAttributes,
            activeAttributeRepositoryIdentifiers);
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof X509CertificateCredential;
    }

    @Override
    protected Map<String, List<Object>> retrievePersonAttributes(final String principalId, final Credential credential,
                                                                 final Optional<Principal> currentPrincipal,
                                                                 final Map<String, List<Object>> queryAttributes) {
        val certificate = ((X509CertificateCredential) credential).getCertificate();
        val certificateAttributes = extractPersonAttributes(certificate);
        queryAttributes.putAll(certificateAttributes);
        val attributes = new LinkedHashMap<String, List<Object>>(
            super.retrievePersonAttributes(principalId, credential, currentPrincipal, queryAttributes));
        attributes.putAll(certificateAttributes);
        return attributes;
    }

    @Override
    protected String extractPrincipalId(final Credential credential, final Optional<Principal> currentPrincipal) {
        return resolvePrincipalInternal(((X509CertificateCredential) credential).getCertificate());
    }

    /**
     * Resolve principal internally, and return the id.
     *
     * @param certificate the certificate
     * @return the string
     */
    protected abstract String resolvePrincipalInternal(X509Certificate certificate);

    /**
     * Get alternate principal if alternate attribute configured.
     *
     * @param certificate X509 Certificate of user
     * @return principal using alternate attribute or null if none configured
     */
    protected String getAlternatePrincipal(final X509Certificate certificate) {
        if (alternatePrincipalAttribute == null) {
            return null;
        }
        val attributes = extractPersonAttributes(certificate);
        val attribute = attributes.get(alternatePrincipalAttribute);
        if (attribute == null) {
            LOGGER.debug("Attempt to get alternate principal with attribute [{}] was unsuccessful.", alternatePrincipalAttribute);
            return null;
        }
        val optionalAttribute = CollectionUtils.firstElement(attribute);
        if (optionalAttribute.isEmpty()) {
            LOGGER.debug("Alternate attribute list for [{}] was empty.", alternatePrincipalAttribute);
            return null;
        }
        val alternatePrincipal = optionalAttribute.get().toString();
        if (StringUtils.isNotEmpty(alternatePrincipal)) {
            LOGGER.debug("Using alternate principal attribute [{}]", alternatePrincipal);
            return alternatePrincipal;
        }
        LOGGER.debug("Returning null principal id...");
        return null;
    }

    /**
     * Get additional attributes from the certificate.
     *
     * @param certificate X509 Certificate of user
     * @return map of attributes
     */
    protected Map<String, List<Object>> extractPersonAttributes(final X509Certificate certificate) {
        val attributes = new LinkedHashMap<String, List<Object>>();

        if (certificate != null) {
            if (StringUtils.isNotBlank(certificate.getSigAlgOID())) {
                attributes.put("sigAlgOid", CollectionUtils.wrapList(certificate.getSigAlgOID()));
            }
            val subjectDn = certificate.getSubjectDN();
            if (subjectDn != null) {
                attributes.put("subjectDn", CollectionUtils.wrapList(subjectDn.getName()));
            }
            val subjectPrincipal = certificate.getSubjectX500Principal();
            if (subjectPrincipal != null) {
                attributes.put("subjectX500Principal", CollectionUtils.wrapList(subjectPrincipal.getName()));
            }
            val issuerDn = certificate.getIssuerDN();
            if (issuerDn != null) {
                attributes.put("issuerDn", CollectionUtils.wrapList(issuerDn.getName()));
            }
            val issuerPrincipal = certificate.getIssuerX500Principal();
            if (issuerPrincipal != null) {
                attributes.put("issuerX500Principal", CollectionUtils.wrapList(issuerPrincipal.getName()));
            }
            try {
                val rfc822Email = getRFC822EmailAddress(certificate.getSubjectAlternativeNames());
                if (rfc822Email != null) {
                    attributes.put("x509Rfc822Email", CollectionUtils.wrapList(rfc822Email));
                }
            } catch (final CertificateParsingException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.warn("Error parsing subject alternative names to get rfc822 email", e);
                }
                LOGGER.warn("Error parsing subject alternative names to get rfc822 email [{}]", e.getMessage());
            }
            try {
                val x509subjectUPN = X509UPNExtractorUtils.extractUPNString(certificate);
                if (x509subjectUPN != null) {
                    attributes.put("x509subjectUPN", CollectionUtils.wrapList(x509subjectUPN));
                }
            } catch (final CertificateParsingException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.warn("Error parsing subject alternative names to get User Principal Name as an attribute", e);
                } else {
                    LOGGER.warn("Error parsing subject alternative names to get User Principal Name as an attribute [{}]", e.getMessage());
                }
            }
        }
        return attributes;
    }

    /**
     * Get Email Address.
     *
     * @param subjectAltNames list of subject alternative name values encoded as collection of Lists with two elements in each List containing type and value.
     * @return String email address or null if the item passed in is not type 1 (rfc822Name)
     * as expected to be returned by implementation of {@code X509Certificate.html#getSubjectAlternativeNames}
     * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.html#getSubjectAlternativeNames()">
     * X509Certificate#getSubjectAlternativeNames</a>
     */
    protected String getRFC822EmailAddress(final Collection<List<?>> subjectAltNames) {
        if (subjectAltNames == null) {
            return null;
        }
        Optional<List<?>> email = subjectAltNames
            .stream()
            .filter(s -> s.size() == 2 && (Integer) s.get(0) == SAN_RFC822_EMAIL_TYPE)
            .findFirst();
        return email.map(objects -> (String) objects.get(1)).orElse(null);
    }
}
