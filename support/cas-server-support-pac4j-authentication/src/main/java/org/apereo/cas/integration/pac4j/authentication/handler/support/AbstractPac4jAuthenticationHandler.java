package org.apereo.cas.integration.pac4j.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;

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
                                                                final CommonProfile profile,
                                                                final BaseClient client) throws GeneralSecurityException {
        if (profile == null) {
            throw new FailedLoginException("Authentication did not produce a user profile for: " + credentials);
        }

        val id = determinePrincipalIdFrom(profile, client);
        if (StringUtils.isBlank(id)) {
            throw new FailedLoginException("No identifier found for this user profile: " + profile);
        }
        credentials.setUserProfile(profile);
        credentials.setTypedIdUsed(isTypedIdUsed);
        val attributes = CoreAuthenticationUtils.convertAttributeValuesToMultiValuedObjects(profile.getAttributes());
        val principal = this.principalFactory.createPrincipal(id, attributes);
        LOGGER.debug("Constructed authenticated principal [{}] based on user profile [{}]", principal, profile);
        return finalizeAuthenticationHandlerResult(credentials, principal, profile, client);
    }

    /**
     * Finalize authentication handler result.
     *
     * @param credentials the credentials
     * @param principal   the principal
     * @param profile     the profile
     * @param client      the client
     * @return the authentication handler execution result
     */
    protected AuthenticationHandlerExecutionResult finalizeAuthenticationHandlerResult(final ClientCredential credentials,
                                                                                       final Principal principal,
                                                                                       final CommonProfile profile,
                                                                                       final BaseClient client) {
        preFinalizeAuthenticationHandlerResult(credentials, principal, profile, client);
        return createHandlerResult(credentials, principal, new ArrayList<>(0));
    }

    /**
     * Pre finalize authentication handler result.
     *
     * @param credentials the credentials
     * @param principal   the principal
     * @param profile     the profile
     * @param client      the client
     */
    protected void preFinalizeAuthenticationHandlerResult(final ClientCredential credentials, final Principal principal,
                                                          final CommonProfile profile, final BaseClient client) {
    }

    /**
     * Determine principal id from profile.
     *
     * @param profile the profile
     * @param client  the client
     * @return the id
     */
    protected String determinePrincipalIdFrom(final CommonProfile profile, final BaseClient client) {
        var id = profile.getId();
        val properties = client != null ? client.getCustomProperties() : new HashMap<>(0);
        if (client != null && properties.containsKey(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_PRINCIPAL_ATTRIBUTE_ID)) {
            val attrObject = properties.get(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_PRINCIPAL_ATTRIBUTE_ID);
            if (attrObject != null) {
                val principalAttribute = attrObject.toString();
                if (profile.containsAttribute(principalAttribute)) {
                    val firstAttribute = CollectionUtils.firstElement(profile.getAttribute(principalAttribute));
                    if (firstAttribute.isPresent()) {
                        id = firstAttribute.get().toString();
                        id = typePrincipalId(id, profile);
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
                val firstAttribute = CollectionUtils.firstElement(profile.getAttribute(principalAttributeId));
                if (firstAttribute.isPresent()) {
                    id = firstAttribute.get().toString();
                    id = typePrincipalId(id, profile);
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

    private String typePrincipalId(final String id, final UserProfile profile) {
        return isTypedIdUsed
            ? profile.getClass().getName() + CommonProfile.SEPARATOR + id
            : id;
    }

    /**
     * Store user profile.
     *
     * @param webContext the web context
     * @param profile    the profile
     */
    protected void storeUserProfile(final WebContext webContext, final CommonProfile profile) {
        val manager = new ProfileManager<CommonProfile>(webContext, webContext.getSessionStore());
        manager.save(true, profile, false);
    }
}
