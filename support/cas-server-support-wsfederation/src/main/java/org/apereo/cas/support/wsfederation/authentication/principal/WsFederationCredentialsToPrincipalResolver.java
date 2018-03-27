package org.apereo.cas.support.wsfederation.authentication.principal;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.services.persondir.IPersonAttributeDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class resolves the principal id regarding the WsFederation credentials.
 *
 * @author John Gasper
 * @since 4.2.0
 */
@Slf4j
@ToString(callSuper = true)
public class WsFederationCredentialsToPrincipalResolver extends PersonDirectoryPrincipalResolver {

    private final WsFederationConfiguration configuration;

    public WsFederationCredentialsToPrincipalResolver(final IPersonAttributeDao attributeRepository,
                                                      final PrincipalFactory principalFactory, final boolean returnNullIfNoAttributes,
                                                      final String principalAttributeName, final WsFederationConfiguration configuration) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes, principalAttributeName);
        this.configuration = configuration;
    }

    /**
     * Extracts the principalId.
     *
     * @param credentials the credentials
     * @return the principal id
     */
    @Override
    protected String extractPrincipalId(final Credential credentials, final Principal currentPrincipal) {
        final WsFederationCredential wsFedCredentials = (WsFederationCredential) credentials;
        final Map<String, List<Object>> attributes = wsFedCredentials.getAttributes();
        LOGGER.debug("Credential attributes provided are: [{}]", attributes);
        final String idAttribute = this.configuration.getIdentityAttribute();
        if (attributes.containsKey(idAttribute)) {
            LOGGER.debug("Extracting principal id from attribute [{}]", this.configuration.getIdentityAttribute());
            final List<Object> idAttributeAsList = attributes.get(this.configuration.getIdentityAttribute());
            if (idAttributeAsList.size() > 1) {
                LOGGER.warn("Found multiple values for id attribute [{}].", idAttribute);
            }
            final String principalId = idAttributeAsList.get(0).toString();
            LOGGER.debug("Principal Id extracted from credentials: [{}]", principalId);
            return principalId;
        }
        LOGGER.warn("Credential attributes do not include an attribute for [{}]. "
            + "This will prohibit CAS to construct a meaningful authenticated principal. "
            + "Examine the released claims and ensure [{}] is allowed", idAttribute, idAttribute);
        return null;
    }

    @Override
    protected Map<String, List<Object>> retrievePersonAttributes(final String principalId, final Credential credential) {
        final WsFederationCredential wsFedCredentials = (WsFederationCredential) credential;
        if (this.configuration.getAttributesType() == WsFederationConfiguration.WsFedPrincipalResolutionAttributesType.WSFED) {
            return wsFedCredentials.getAttributes();
        }
        if (this.configuration.getAttributesType() == WsFederationConfiguration.WsFedPrincipalResolutionAttributesType.CAS) {
            return super.retrievePersonAttributes(principalId, credential);
        }
        final Map<String, List<Object>> mergedAttributes = new HashMap<>(wsFedCredentials.getAttributes());
        mergedAttributes.putAll(super.retrievePersonAttributes(principalId, credential));
        return mergedAttributes;
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null && WsFederationCredential.class.isAssignableFrom(credential.getClass());
    }
}
