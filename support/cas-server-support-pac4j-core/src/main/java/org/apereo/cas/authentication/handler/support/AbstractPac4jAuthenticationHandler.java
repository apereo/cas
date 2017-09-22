package org.apereo.cas.authentication.handler.support;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.pac4j.core.profile.UserProfile;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * Abstract pac4j authentication handler which builds the CAS handler result from the pac4j user profile.
 *
 * @author Jerome Leleu
 * @since 4.1.0
 */
public abstract class AbstractPac4jAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private boolean isTypedIdUsed;

    public AbstractPac4jAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
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
     * @throws PreventedException       On the indeterminate case when authentication is prevented.
     */
    protected HandlerResult createResult(final ClientCredential credentials, final UserProfile profile)
            throws GeneralSecurityException, PreventedException {

        if (profile != null) {
            final String id;
            if (isTypedIdUsed) {
                id = profile.getTypedId();
            } else {
                id = profile.getId();
            }
            if (StringUtils.isNotBlank(id)) {
                credentials.setUserProfile(profile);
                credentials.setTypedIdUsed(isTypedIdUsed);
                return new DefaultHandlerResult(
                        this,
                        new BasicCredentialMetaData(credentials),
                        this.principalFactory.createPrincipal(id, profile.getAttributes()));
            }

            throw new FailedLoginException("No identifier found for this user profile: " + profile);
        }

        throw new FailedLoginException("Authentication did not produce a user profile for: " + credentials);
    }

    public void setTypedIdUsed(final boolean typedIdUsed) {
        this.isTypedIdUsed = typedIdUsed;
    }
}
