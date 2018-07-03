package org.apereo.cas.adaptors.authy;

import com.authy.api.Token;
import com.authy.api.User;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Authy authentication handler for CAS.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class AuthyAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private final boolean forceVerification;
    private final AuthyClientInstance instance;

    public AuthyAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                      final AuthyClientInstance instance, final boolean forceVerification) {
        super(name, servicesManager, principalFactory, null);
        this.instance = instance;
        this.forceVerification = forceVerification;
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        final var tokenCredential = (AuthyTokenCredential) credential;

        final var authentication = WebUtils.getInProgressAuthentication();
        if (authentication == null) {
            throw new IllegalArgumentException("CAS has no reference to an authentication event to locate a principal");
        }
        final var principal = authentication.getPrincipal();

        final var user = instance.getOrCreateUser(principal);
        if (!user.isOk()) {
            throw new FailedLoginException(AuthyClientInstance.getErrorMessage(user.getError()));
        }

        final Map<String, String> options = new HashMap<>(1);
        options.put("force", Boolean.toString(this.forceVerification));

        final var verification = verifyAuthyToken(tokenCredential, user, options);
        if (!verification.isOk()) {
            throw new FailedLoginException(AuthyClientInstance.getErrorMessage(verification.getError()));
        }

        return createHandlerResult(tokenCredential, principal, new ArrayList<>());
    }

    @SneakyThrows
    private Token verifyAuthyToken(final AuthyTokenCredential tokenCredential, final User user, final Map<String, String> options) {
        return this.instance.getAuthyTokens()
            .verify(user.getId(), tokenCredential.getToken(), options);
    }

    @Override
    public boolean supports(final Credential credential) {
        return AuthyTokenCredential.class.isAssignableFrom(credential.getClass());
    }
}
