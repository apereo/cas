package org.apereo.cas.okta;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.okta.OktaAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LoggingUtils;

import com.okta.authn.sdk.client.AuthenticationClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.Objects;

/**
 * This is {@link OktaAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@Getter
public class OktaAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private final OktaAuthenticationProperties properties;

    private final AuthenticationClient oktaAuthenticationClient;

    public OktaAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                     final PrincipalFactory principalFactory,
                                     final OktaAuthenticationProperties properties,
                                     final AuthenticationClient oktaAuthenticationClient) {
        super(name, servicesManager, principalFactory, properties.getOrder());
        this.properties = properties;
        this.oktaAuthenticationClient = oktaAuthenticationClient;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword)
        throws GeneralSecurityException {

        try {
            val username = credential.getUsername();
            val adapter = new OktaAuthenticationStateHandlerAdapter(getPasswordPolicyHandlingStrategy(), getPasswordPolicyConfiguration());
            val response = oktaAuthenticationClient.authenticate(username, credential.getPassword().toCharArray(), null, adapter);
            Objects.requireNonNull(response, "Authentication response cannot be null");
            adapter.throwExceptionIfNecessary();
            LOGGER.debug("Created principal for id [{}] and [{}] attributes", adapter.getUsername(), adapter.getUserAttributes());
            val principal = this.principalFactory.createPrincipal(adapter.getUsername(), adapter.getUserAttributes());
            return createHandlerResult(credential, principal, adapter.getWarnings());
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            throw new FailedLoginException("Invalid credentials: " + e.getMessage());
        }
    }

}
