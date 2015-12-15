package org.jasig.cas.integration.pac4j.authentication.handler.support;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.support.AbstractPac4jAuthenticationHandler;
import org.jasig.cas.authentication.principal.ClientCredential;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.http.credentials.authenticator.Authenticator;
import org.pac4j.http.profile.creator.AuthenticatorProfileCreator;
import org.pac4j.http.profile.creator.ProfileCreator;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * Abstract pac4j authentication handler which uses a pac4j authenticator and profile creator.
 *
 * @author Jerome Leleu
 * @param <I> the type parameter
 * @param <C> the type parameter
 * @since 4.2.0
 */
public abstract class AbstractWrapperAuthenticationHandler<I extends Credential, C extends Credentials>
        extends AbstractPac4jAuthenticationHandler {

    /**
     * The pac4j profile creator used for authentication.
     */
    protected ProfileCreator profileCreator = AuthenticatorProfileCreator.INSTANCE;

    @Override
    public final boolean supports(final Credential credential) {
        return credential != null && getCasCredentialsType().isAssignableFrom(credential.getClass());
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
        CommonHelper.assertNotNull("profileCreator", this.profileCreator);

        final C credentials = convertToPac4jCredentials((I) credential);
        logger.debug("credentials: {}", credentials);

        try {
            final Authenticator authenticator = getAuthenticator(credential);
            CommonHelper.assertNotNull("authenticator", authenticator);
            authenticator.validate(credentials);
        } catch (final CredentialsException e) {
            logger.error("Failed to validate credentials", e);
            throw new FailedLoginException("Failed to validate credentials: " + e.getMessage());
        }

        final UserProfile profile = profileCreator.create(credentials);
        logger.debug("profile: {}", profile);

        return createResult(new ClientCredential(credentials), profile);
    }

    /**
     * Convert a CAS credential into a pac4j credentials to play the authentication.
     *
     * @param casCredential the CAS credential
     * @return the pac4j credentials
     * @throws GeneralSecurityException On authentication failure.
     * @throws PreventedException On the indeterminate case when authentication is prevented.
     */
    protected abstract C convertToPac4jCredentials(final I casCredential) throws GeneralSecurityException,
            PreventedException;

    /**
     * Return the CAS credential supported by this handler (to be converted in a pac4j credentials
     * by {@link #convertToPac4jCredentials(Credential)}).
     *
     * @return the CAS credential class
     */
    protected abstract Class<I> getCasCredentialsType();

    /**
     * Gets authenticator.
     *
     * @param credential the credential
     * @return the authenticator
     */
    protected abstract Authenticator getAuthenticator(final Credential credential);

    public ProfileCreator getProfileCreator() {
        return profileCreator;
    }

    public void setProfileCreator(final ProfileCreator profileCreator) {
        this.profileCreator = profileCreator;
    }
}
