package org.apereo.cas.adaptors.generic.remote;

import module java.base;
import org.apereo.cas.authentication.AbstractAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.generic.RemoteAuthenticationProperties;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link RemoteCookieAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class RemoteCookieAuthenticationHandler extends AbstractAuthenticationHandler {
    private final CipherExecutor<Serializable, String> remoteCookieCipherExecutor;

    public RemoteCookieAuthenticationHandler(final RemoteAuthenticationProperties props,

                                             final PrincipalFactory principalFactory,
                                             final CipherExecutor remoteCookieCipherExecutor) {
        super(props.getName(), principalFactory, props.getOrder());
        this.remoteCookieCipherExecutor = remoteCookieCipherExecutor;
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof RemoteAuthenticationCredential;
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return RemoteAuthenticationCredential.class.isAssignableFrom(clazz);
    }

    /**
     * Authenticate the principal named by the cookie. A cookie that the cipher cannot decode yields no principal
     * id rather than an error (a signature that cannot be verified is refused that way), and is rejected like a
     * cookie that fails to decrypt.
     *
     * @param credential the credential
     * @param service    the service
     * @return the authentication handler execution result
     * @throws Throwable the throwable
     */
    @Override
    public AuthenticationHandlerExecutionResult authenticate(final Credential credential, final Service service) throws Throwable {
        try {
            val addressCredential = (RemoteAuthenticationCredential) credential;
            val principalId = remoteCookieCipherExecutor.decode(addressCredential.getCookie());
            if (StringUtils.isNotBlank(principalId)) {
                return new DefaultAuthenticationHandlerExecutionResult(this, addressCredential,
                    principalFactory.createPrincipal(principalId));
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        throw new FailedLoginException("Unable to accept cookie for authentication");
    }
}
