package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.http.credentials.authenticator.X509Authenticator;
import java.util.Optional;

/**
 * This is {@link OAuth20X509Authenticator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class OAuth20X509Authenticator extends X509Authenticator {
    private final ServicesManager servicesManager;

    private final OAuth20RequestParameterResolver requestParameterResolver;

    @Override
    public Optional<Credentials> validate(final CallContext ctx, final Credentials credentials) {
        val clientIdAndSecret = requestParameterResolver.resolveClientIdAndClientSecret(ctx);
        val clientId = clientIdAndSecret.getKey();
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
        if (!OAuth20Utils.isTokenAuthenticationMethodSupportedFor(ctx, registeredService, "tls_client_auth")) {
            LOGGER.warn("TLS authentication method is not supported for service [{}]", registeredService.getName());
            return Optional.empty();
        }
        return super.validate(ctx, credentials);
    }
}
