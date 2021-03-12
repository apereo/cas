package org.apereo.cas.support.wsfederation.authentication.principal;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.util.CollectionUtils;

import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class resolves the principal id regarding the WsFederation credentials.
 *
 * @author John Gasper
 * @since 4.2.0
 */
@Slf4j
@Setter
@ToString(callSuper = true)
public class WsFederationCredentialsToPrincipalResolver extends PersonDirectoryPrincipalResolver {
    private WsFederationConfiguration configuration;

    public WsFederationCredentialsToPrincipalResolver(final PrincipalResolutionContext context) {
        super(context);
    }

    /**
     * Extracts the principalId.
     *
     * @param credentials the credentials
     * @return the principal id
     */
    @Override
    protected String extractPrincipalId(final Credential credentials, final Optional<Principal> currentPrincipal) {
        val wsFedCredentials = (WsFederationCredential) credentials;
        val attributes = wsFedCredentials.getAttributes();
        LOGGER.debug("Credential attributes provided are: [{}]", attributes);
        val idAttribute = this.configuration.getIdentityAttribute();
        if (attributes.containsKey(idAttribute)) {
            LOGGER.debug("Extracting principal id from attribute [{}]", this.configuration.getIdentityAttribute());
            val idAttributeAsList = CollectionUtils.toCollection(attributes.get(this.configuration.getIdentityAttribute()));
            if (idAttributeAsList.size() > 1) {
                LOGGER.warn("Found multiple values for id attribute [{}].", idAttribute);
            } else {
                LOGGER.debug("Found principal id attribute as [{}]", idAttributeAsList);
            }

            val result = CollectionUtils.firstElement(idAttributeAsList);
            if (result.isPresent()) {
                val principalId = result.get().toString();
                LOGGER.debug("Principal Id extracted from credentials: [{}]", principalId);
                return principalId;
            }
        }
        LOGGER.warn("Credential attributes do not include an attribute for [{}]. "
            + "This will prohibit CAS to construct a meaningful authenticated principal. "
            + "Examine the released claims and ensure [{}] is allowed", idAttribute, idAttribute);
        return null;
    }

    @Override
    protected Map<String, List<Object>> retrievePersonAttributes(final String principalId, final Credential credential,
                                                                 final Optional<Principal> currentPrincipal,
                                                                 final Map<String, List<Object>> queryAttributes) {
        val wsFedCredentials = (WsFederationCredential) credential;
        if (this.configuration.getAttributesType() == WsFederationConfiguration.WsFedPrincipalResolutionAttributesType.WSFED) {
            return wsFedCredentials.getAttributes();
        }
        if (this.configuration.getAttributesType() == WsFederationConfiguration.WsFedPrincipalResolutionAttributesType.CAS) {
            return super.retrievePersonAttributes(principalId, credential, currentPrincipal, new HashMap<>());
        }
        val mergedAttributes = new HashMap<>(wsFedCredentials.getAttributes());
        mergedAttributes.putAll(super.retrievePersonAttributes(principalId, credential, currentPrincipal, new HashMap<>()));
        return mergedAttributes;
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null && WsFederationCredential.class.isAssignableFrom(credential.getClass());
    }
}
