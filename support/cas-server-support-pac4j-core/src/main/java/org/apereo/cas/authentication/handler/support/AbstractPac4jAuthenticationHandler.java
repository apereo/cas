package org.apereo.cas.authentication.handler.support;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.pac4j.core.profile.UserProfile;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import lombok.Setter;

/**
 * Abstract pac4j authentication handler which builds the CAS handler result from the pac4j user profile.
 *
 * @author Jerome Leleu
 * @since 4.1.0
 */
@Slf4j
@Setter
public abstract class AbstractPac4jAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private boolean isTypedIdUsed;

    public AbstractPac4jAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory, final Integer order) {
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
        final Principal principal = this.principalFactory.createPrincipal(id, profile.getAttributes());
        LOGGER.debug("Constructed authenticated principal [{}] based on user profile [{}]", principal, profile);
        return createHandlerResult(credentials, principal, new ArrayList<>(0));
    }
}
