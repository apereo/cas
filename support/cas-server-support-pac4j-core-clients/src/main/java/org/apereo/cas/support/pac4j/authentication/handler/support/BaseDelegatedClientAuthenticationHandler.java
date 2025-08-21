package org.apereo.cas.support.pac4j.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.CollectionUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.BasicUserProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.Pac4jConstants;
import javax.security.auth.login.FailedLoginException;
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
public abstract class BaseDelegatedClientAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    /**
     * The session store.
     */
    protected final SessionStore sessionStore;

    private String principalAttributeId;

    private boolean isTypedIdUsed;

    protected BaseDelegatedClientAuthenticationHandler(final String name,
                                                       final PrincipalFactory principalFactory, final Integer order,
                                                       final SessionStore sessionStore) {
        super(name, principalFactory, order);
        this.sessionStore = sessionStore;
    }

    protected AuthenticationHandlerExecutionResult createResult(final ClientCredential credentials,
                                                                final UserProfile profile,
                                                                final BaseClient client,
                                                                final Service service) throws Throwable {
        if (profile == null) {
            throw new FailedLoginException("Authentication did not produce a user profile for: " + credentials);
        }

        val id = determinePrincipalIdFrom(profile, client);
        if (StringUtils.isBlank(id)) {
            throw new FailedLoginException("No identifier found for this user profile: " + profile);
        }
        credentials.setUserProfile(profile);
        credentials.setTypedIdUsed(isTypedIdUsed);
        val attributes = CollectionUtils.toMultiValuedMap(profile.getAttributes());
        attributes.put(Pac4jConstants.CLIENT_NAME, CollectionUtils.wrap(profile.getClientName()));
        if (profile instanceof final BasicUserProfile bup) {
            attributes.putAll(CollectionUtils.toMultiValuedMap(bup.getAuthenticationAttributes()));
        }
        val initialPrincipal = principalFactory.createPrincipal(id, attributes);
        val principal = finalizeAuthenticationPrincipal(initialPrincipal, client, credentials, service);
        LOGGER.debug("Constructed authenticated principal [{}] based on user profile [{}]", principal, profile);
        return finalizeAuthenticationHandlerResult(credentials, principal, profile, client, service);
    }

    protected Principal finalizeAuthenticationPrincipal(final Principal initialPrincipal, final BaseClient client,
                                                        final ClientCredential credentials, final Service service) throws Throwable {
        return initialPrincipal;
    }

    protected AuthenticationHandlerExecutionResult finalizeAuthenticationHandlerResult(final ClientCredential credentials,
                                                                                       final Principal principal,
                                                                                       final UserProfile profile,
                                                                                       final BaseClient client,
                                                                                       final Service service) throws Throwable {
        preFinalizeAuthenticationHandlerResult(credentials, principal, profile, client, service);
        val result = createHandlerResult(credentials, principal, new ArrayList<>());
        return postFinalizeAuthenticationHandlerResult(result, credentials, principal, client, service);
    }

    protected AuthenticationHandlerExecutionResult postFinalizeAuthenticationHandlerResult(final AuthenticationHandlerExecutionResult result,
                                                                                           final ClientCredential credentials,
                                                                                           final Principal principal,
                                                                                           final BaseClient client,
                                                                                           final Service service) {
        return result;
    }

    protected void preFinalizeAuthenticationHandlerResult(final ClientCredential credentials, final Principal principal,
                                                          final UserProfile profile, final BaseClient client,
                                                          final Service service) throws Throwable {
    }

    /**
     * Determine principal id from profile.
     *
     * @param profile the profile
     * @param client  the client
     * @return the id
     */
    protected String determinePrincipalIdFrom(final UserProfile profile, final BaseClient client) {
        var id = profile.getId();
        val properties = client != null ? client.getCustomProperties() : new HashMap<>();
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
                    LOGGER.debug("Authentication indicates usage of client principal attribute [{}] for the identifier [{}]", principalAttribute, id);
                } else {
                    LOGGER.warn("Authentication cannot find attribute [{}] to use as principal id", principalAttribute);
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
                    + "Either adjust the CAS configuration to use a different attribute, or contact the authentication provider noted by [{}] "
                    + "to release the expected attribute to CAS", principalAttributeId, profile.getAttributes());
            }
            LOGGER.debug("Authentication indicates usage of attribute [{}] for the identifier [{}]", principalAttributeId, id);
        } else if (isTypedIdUsed) {
            id = profile.getTypedId();
            LOGGER.debug("Authentication indicates usage of typed profile id [{}]", id);
        }
        LOGGER.debug("Final principal id determined based on client [{}] and user profile [{}] is [{}]", profile, client, id);
        return id;
    }

    private String typePrincipalId(final String id, final UserProfile profile) {
        return isTypedIdUsed
            ? profile.getClass().getName() + Pac4jConstants.TYPED_ID_SEPARATOR + id
            : id;
    }

    /**
     * Store user profile.
     *
     * @param webContext the web context
     * @param profile    the profile
     */
    protected void storeUserProfile(final WebContext webContext, final UserProfile profile) {
        val manager = new ProfileManager(webContext, this.sessionStore);
        manager.save(true, profile, false);
    }
}
