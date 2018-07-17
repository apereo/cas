package org.apereo.cas.integration.pac4j.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.profile.UserProfile;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

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
     * @return the built handler result
     * @throws GeneralSecurityException On authentication failure.
     */
    protected AuthenticationHandlerExecutionResult createResult(final ClientCredential credentials, final UserProfile profile) throws GeneralSecurityException {
        if (profile == null) {
            throw new FailedLoginException("Authentication did not produce a user profile for: " + credentials);
        }

        val id = determinePrincipalIdFrom(profile);
        if (StringUtils.isBlank(id)) {
            throw new FailedLoginException("No identifier found for this user profile: " + profile);
        }
        credentials.setUserProfile(profile);
        credentials.setTypedIdUsed(isTypedIdUsed);
        val principal = this.principalFactory.createPrincipal(id, new LinkedHashMap<>(profile.getAttributes()));
        LOGGER.debug("Constructed authenticated principal [{}] based on user profile [{}]", principal, profile);
        return createHandlerResult(credentials, principal, new ArrayList<>(0));
    }

    /**
     * Determine principal id from profile.
     *
     * @param profile the profile
     * @return the id
     */
    protected String determinePrincipalIdFrom(final UserProfile profile) {
        if (StringUtils.isNotBlank(principalAttributeId) && profile.containsAttribute(principalAttributeId)) {
            val id = profile.getAttribute(principalAttributeId).toString();
            LOGGER.debug("Delegated authentication indicates usage of attribute [{}] for the identifier [{}]", principalAttributeId, id);
            return id;
        }

        if (isTypedIdUsed) {
            val id = profile.getTypedId();
            LOGGER.debug("Delegated authentication indicates usage of typed profile id [{}]", id);
            return id;
        }
        return profile.getId();
    }
}
