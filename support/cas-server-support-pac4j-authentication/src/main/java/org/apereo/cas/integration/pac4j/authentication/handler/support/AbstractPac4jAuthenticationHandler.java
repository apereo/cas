package org.apereo.cas.integration.pac4j.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.profile.UserProfile;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract pac4j authentication handler which builds the CAS handler result from the pac4j user profile.
 *
 * @author Jerome Leleu
 * @since 4.1.0
 */
@Slf4j
@Setter
public abstract class AbstractPac4jAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    private String principalAttributeId;
    private boolean isTypedIdUsed;

    public AbstractPac4jAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                              final PrincipalFactory principalFactory, final Integer order) {
        super(name, servicesManager, principalFactory, order);
    }

    /**
     * Build the handler result.
     *
     * @param credentials the provided credentials
     * @param profile     the retrieved user profile
     * @param client      the client
     * @return the built handler result
     * @throws GeneralSecurityException On authentication failure.
     */
    protected AuthenticationHandlerExecutionResult createResult(final ClientCredential credentials,
                                                                final UserProfile profile,
                                                                final BaseClient client) throws GeneralSecurityException {
        if (profile == null) {
            throw new FailedLoginException("Authentication did not produce a user profile for: " + credentials);
        }

        final String id = determinePrincipalIdFrom(profile, client);
        if (StringUtils.isBlank(id)) {
            throw new FailedLoginException("No identifier found for this user profile: " + profile);
        }
        credentials.setUserProfile(profile);
        credentials.setTypedIdUsed(isTypedIdUsed);
        final Principal principal = this.principalFactory.createPrincipal(id, new LinkedHashMap<>(profile.getAttributes()));
        LOGGER.debug("Constructed authenticated principal [{}] based on user profile [{}]", principal, profile);
        return createHandlerResult(credentials, principal, new ArrayList<>(0));
    }

    /**
     * Determine principal id from profile.
     *
     * @param profile the profile
     * @param client  the client
     * @return the id
     */
    protected String determinePrincipalIdFrom(final UserProfile profile, final BaseClient client) {
        String id = profile.getId();
        final Map properties = client != null ? client.getCustomProperties() : new HashMap<>();

        if (properties.containsKey(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_PRINCIPAL_ATTRIBUTE_ID)) {
            final Object attrObject = properties.get(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_PRINCIPAL_ATTRIBUTE_ID);
            if (attrObject != null) {
                final String principalAttribute = attrObject.toString();
                if (profile.containsAttribute(principalAttribute)) {
                    final Optional<Object> firstAttribute = CollectionUtils.firstElement(profile.getAttribute(principalAttribute));
                    if (firstAttribute.isPresent()) {
                        id = firstAttribute.get().toString();
                    }
                    LOGGER.debug("Delegated authentication indicates usage of client principal attribute [{}] for the identifier [{}]", principalAttribute, id);
                } else {
                    LOGGER.warn("Delegated authentication cannot find attribute [{}] to use as principal id", principalAttribute);
                }
            } else {
                LOGGER.warn("No custom principal attribute was provided by the client [{}]. Using the default id [{}]", client, id);
            }
        } else if (StringUtils.isNotBlank(principalAttributeId)) {
            if (profile.containsAttribute(principalAttributeId)) {
                final Optional<Object> firstAttribute = CollectionUtils.firstElement(profile.getAttribute(principalAttributeId));
                if (firstAttribute.isPresent()) {
                    id = firstAttribute.get().toString();
                }
            } else {
                LOGGER.warn("CAS cannot use [{}] as the principal attribute id, since the profile attributes do not contain the attribute. "
                    + "Either adjust the CAS configuration to use a different attribute, or contact the delegated authentication provider noted by [{}] "
                    + "to release the expected attribute to CAS", principalAttributeId, profile.getAttributes());
            }
            LOGGER.debug("Delegated authentication indicates usage of attribute [{}] for the identifier [{}]", principalAttributeId, id);
        } else if (isTypedIdUsed) {
            id = profile.getTypedId();
            LOGGER.debug("Delegated authentication indicates usage of typed profile id [{}]", id);
        }
        LOGGER.debug("Final principal id determined based on client [{}] and user profile [{}] is [{}]", profile, client, id);
        return id;
    }
}
