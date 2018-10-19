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

import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;



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

    private String alternatePrincipalAttribute;

    public AbstractX509PrincipalResolver(final IPersonAttributeDao attributeRepository,
                                         final PrincipalFactory principalFactory, final boolean returnNullIfNoAttributes,
                                         final String principalAttributeName,
                                         final String alternatePrincipalAttribute) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes, principalAttributeName);
        this.alternatePrincipalAttribute = alternatePrincipalAttribute;
    }

    public AbstractX509PrincipalResolver(final IPersonAttributeDao attributeRepository,
                                         final PrincipalFactory principalFactory, final boolean returnNullIfNoAttributes,
                                         final String principalAttributeName) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes, principalAttributeName);
    }

    @Override
    protected String extractPrincipalId(final Credential credential, final Optional<Principal> currentPrincipal) {
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

    /**
     * Get alternate principal if alternate attribute configured.
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
            LOGGER.debug("Attempt to get alternate principal with attribute {} was unsuccessful.", alternatePrincipalAttribute);
            return null;
        }
        val optionalAttribute = CollectionUtils.firstElement(attribute);
        if (!optionalAttribute.isPresent()) {
            LOGGER.debug("Alternate attribute list for {} was empty.", alternatePrincipalAttribute);
            return null;
        }
        val alternatePrincipal = optionalAttribute.get().toString();
        if (StringUtils.isNotEmpty(alternatePrincipal)) {
            LOGGER.debug("Using alternate principal attribute {} ", alternatePrincipal);
            return alternatePrincipal;
        }
        LOGGER.debug("Returning null principal id...");
        return null;
    }

    /**
     * Get additional attributes from the certificate.
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
        }
        return attributes;
    }

}
