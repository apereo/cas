package org.apereo.cas.token.authentication;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.token.TokenAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.Assert;
import java.util.ArrayList;

/**
 * This is {@link TokenAuthenticationHandler} that authenticates instances of {@link TokenCredential}.
 * There is no need for a separate {@link PrincipalResolver} component
 * as this handler will auto-populate the principal attributes itself.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
public class TokenAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    private final ServicesManager servicesManager;

    public TokenAuthenticationHandler(final ServicesManager servicesManager,
                                      final PrincipalFactory principalFactory,
                                      final TokenAuthenticationProperties properties) {
        super(properties.getName(), principalFactory, properties.getOrder());
        this.servicesManager = servicesManager;
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(
        final Credential credential, final Service service) throws PreventedException {
        try {
            val tokenCredential = (BasicIdentifiableCredential) credential;
            val registeredService = servicesManager.findServiceBy(service);
            val profile = TokenAuthenticationSecurity.forRegisteredService(registeredService).validateToken(tokenCredential.getId());
            Assert.notNull(profile, "Authentication attempt failed to produce an authenticated profile");
            val attributes = CollectionUtils.toMultiValuedMap(profile.getAttributes());
            val principal = principalFactory.createPrincipal(profile.getId(), attributes);
            tokenCredential.setId(principal.getId());
            return createHandlerResult(tokenCredential, principal, new ArrayList<>());
        } catch (final Throwable e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof BasicIdentifiableCredential;
    }

}
