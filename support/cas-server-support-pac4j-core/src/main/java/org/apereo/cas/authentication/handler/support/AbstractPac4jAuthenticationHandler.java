package org.apereo.cas.authentication.handler.support;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;

/**
 * Abstract pac4j authentication handler which builds the CAS handler result from the pac4j user profile.
 *
 * @author Jerome Leleu
 * @since 4.1.0
 */
public abstract class AbstractPac4jAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPac4jAuthenticationHandler.class);
    
    private boolean isTypedIdUsed;

    public AbstractPac4jAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                              final PrincipalFactory principalFactory,
                                              final Integer order) {
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
    protected HandlerResult createResult(final ClientCredential credentials, final UserProfile profile)
            throws GeneralSecurityException {

        if (profile == null) {
            throw new FailedLoginException("Authentication did not produce a user profile for: " + credentials);
        }

        final String id;
        if (isTypedIdUsed) {
            id = profile.getTypedId();
            LOGGER.debug("Delegated authentication indicates usage of typed profile id [{}]", id);
        } else {
            id = profile.getId();
        }

        if (StringUtils.isBlank(id)) {
            throw new FailedLoginException("No identifier found for this user profile: " + profile);
        }

        credentials.setUserProfile(profile);
        credentials.setTypedIdUsed(isTypedIdUsed);
        
        final Principal principal = this.principalFactory.createPrincipal(id, new LinkedHashMap<>(profile.getAttributes()));
        LOGGER.debug("Constructed authenticated principal [{}] based on user profile [{}]", principal, profile);
        return createHandlerResult(credentials, principal, null);
    }

    public void setTypedIdUsed(final boolean typedIdUsed) {
        this.isTypedIdUsed = typedIdUsed;
    }
}
