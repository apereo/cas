package org.jasig.cas.authentication.handler.support;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.principal.ClientCredential;
import org.pac4j.core.profile.UserProfile;
import org.springframework.beans.factory.annotation.Value;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * Abstract pac4j authentication handler which builds the CAS handler result from the pac4j user profile.
 *
 * @author Jerome Leleu
 * @since 4.1.0
 */
public abstract class AbstractPac4jAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    /**
     * Whether to use the typed identifier (by default) or just the identifier.
     */
    @Value("${cas.pac4j.client.authn.typedidused:true}")
    private boolean typedIdUsed = true;

    /**
     * Build the handler result.
     *
     * @param credentials the provided credentials
     * @param profile the retrieved user profile
     * @return the built handler result
     * @throws GeneralSecurityException On authentication failure.
     * @throws PreventedException On the indeterminate case when authentication is prevented.
     */
    protected HandlerResult createResult(final ClientCredential credentials, final UserProfile profile)
            throws GeneralSecurityException, PreventedException {

        if (profile != null) {
            final String id;
            if (typedIdUsed) {
                id = profile.getTypedId();
            } else {
                id = profile.getId();
            }
            if (StringUtils.isNotBlank(id)) {
                credentials.setUserProfile(profile);
                credentials.setTypedIdUsed(typedIdUsed);
                return new DefaultHandlerResult(
                        this,
                        new BasicCredentialMetaData(credentials),
                        this.principalFactory.createPrincipal(id, profile.getAttributes()));
            }

            throw new FailedLoginException("No identifier found for this user profile: " + profile);
        }

        throw new FailedLoginException("Authentication did not produce a user profile for: " + credentials);
    }

    public boolean isTypedIdUsed() {
        return typedIdUsed;
    }

    public void setTypedIdUsed(final boolean typedIdUsed) {
        this.typedIdUsed = typedIdUsed;
    }
}
