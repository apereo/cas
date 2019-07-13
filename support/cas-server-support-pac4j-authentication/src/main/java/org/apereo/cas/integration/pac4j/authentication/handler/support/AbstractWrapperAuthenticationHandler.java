package org.apereo.cas.integration.pac4j.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.creator.AuthenticatorProfileCreator;
import org.pac4j.core.profile.creator.ProfileCreator;
import org.pac4j.core.util.InitializableObject;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;

/**
 * Abstract pac4j authentication handler which uses a pac4j authenticator and profile creator.
 *
 * @param <I> the type parameter
 * @param <C> the type parameter
 * @author Jerome Leleu
 * @since 4.2.0
 */
@Slf4j
@Setter
public abstract class AbstractWrapperAuthenticationHandler<I extends Credential, C extends Credentials> extends AbstractPac4jAuthenticationHandler {

    /**
     * The pac4j profile creator used for authentication.
     */
    protected @NonNull ProfileCreator<C> profileCreator = AuthenticatorProfileCreator.INSTANCE;

    public AbstractWrapperAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory, final Integer order) {
        super(name, servicesManager, principalFactory, order);
    }

    /**
     * Gets the web context from the current thread-bound object.
     *
     * @return the web context
     */
    protected static WebContext getWebContext() {
        return new JEEContext(HttpRequestUtils.getHttpServletRequestFromRequestAttributes(),
            HttpRequestUtils.getHttpServletResponseFromRequestAttributes(),
            new JEESessionStore());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null && getCasCredentialsType().isAssignableFrom(credential.getClass());
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        val credentials = convertToPac4jCredentials((I) credential);
        LOGGER.trace("Credentials converted to [{}]", credentials);
        try {
            val authenticator = getAuthenticator(credential);
            if (authenticator instanceof InitializableObject) {
                LOGGER.trace("Initializing authenticator [{}]", authenticator);
                ((InitializableObject) authenticator).init();
            }
            val webContext = getWebContext();
            LOGGER.trace("Validating credentials [{}] using authenticator [{}]", credentials, authenticator);
            authenticator.validate(credentials, webContext);

            LOGGER.trace("Creating user profile result for [{}]", credentials);
            val profileResult = this.profileCreator.create(credentials, webContext);
            if (profileResult.isEmpty()) {
                throw new FailedLoginException("Unable to create common profile instance for credential " + credential);
            }
            val profile = CommonProfile.class.cast(profileResult.get());
            LOGGER.debug("Authenticated profile: [{}]", profile);
            val clientCredential = new ClientCredential(credentials, authenticator.getClass().getSimpleName());
            return createResult(clientCredential, profile, null);
        } catch (final Exception e) {
            LOGGER.error("Failed to validate credentials", e);
            throw new FailedLoginException("Failed to validate credentials: " + e.getMessage());
        }
    }

    /**
     * Convert a CAS credential into a pac4j credentials to play the authentication.
     *
     * @param casCredential the CAS credential
     * @return the pac4j credentials
     * @throws GeneralSecurityException On authentication failure.
     */
    protected abstract C convertToPac4jCredentials(I casCredential) throws GeneralSecurityException;

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
    protected abstract Authenticator<C> getAuthenticator(Credential credential);
}
