package org.apereo.cas.adaptors.x509.authentication.principal;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.util.CollectionUtils;

import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

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
@Slf4j
@Setter
public abstract class AbstractX509PrincipalResolver extends PersonDirectoryPrincipalResolver {

    private String alternatePrincipalAttribute;

    private X509AttributeExtractor x509AttributeExtractor;

    protected AbstractX509PrincipalResolver(final PrincipalResolutionContext context) {
        super(context);
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
        val attributes = new LinkedHashMap<>(
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
        if (StringUtils.isBlank(alternatePrincipalAttribute)) {
            return null;
        }
        val attributes = extractPersonAttributes(certificate);
        val attribute = attributes.get(alternatePrincipalAttribute);
        if (attribute == null) {
            LOGGER.debug("Attempt to get alternate principal with attribute [{}] was unsuccessful.", alternatePrincipalAttribute);
            return null;
        }
        val optionalAttribute = CollectionUtils.firstElement(attribute);
        return optionalAttribute
            .map(Object::toString)
            .filter(StringUtils::isNotEmpty)
            .map(alternatePrincipal -> {
                LOGGER.debug("Using alternate principal attribute [{}]", alternatePrincipal);
                return alternatePrincipal;
            }).orElseGet(() -> {
                LOGGER.trace("Returning null principal id...");
                return null;
            });
    }

    /**
     * Extract various attributes from the certificate about the person.
     * This method is here for backwards compatibility with deployments that overrode this method.
     *
     * @param certificate X509 Certificate
     * @return Map of the attributes
     */
    protected Map<String, List<Object>> extractPersonAttributes(final X509Certificate certificate) {
        return x509AttributeExtractor.extractPersonAttributes(certificate);
    }

}
