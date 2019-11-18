package org.apereo.cas.adaptors.authy;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;

import com.authy.api.Token;
import com.authy.api.User;
import lombok.SneakyThrows;
import lombok.val;

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
public class AuthyAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private final boolean forceVerification;
    private final AuthyClientInstance instance;

    public AuthyAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                      final AuthyClientInstance instance, final boolean forceVerification,
                                      final Integer order) {
        super(name, servicesManager, principalFactory, order);
        this.instance = instance;
        this.forceVerification = forceVerification;
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        val tokenCredential = (AuthyTokenCredential) credential;

        val authentication = WebUtils.getInProgressAuthentication();
        if (authentication == null) {
            throw new IllegalArgumentException("CAS has no reference to an authentication event to locate a principal");
        }
        val principal = authentication.getPrincipal();

        val user = instance.getOrCreateUser(principal);
        if (!user.isOk()) {
            throw new FailedLoginException(AuthyClientInstance.getErrorMessage(user.getError()));
        }

        val options = new HashMap<String, String>(1);
        options.put("force", Boolean.toString(this.forceVerification));

        val verification = verifyAuthyToken(tokenCredential, user, options);
        if (!verification.isOk()) {
            throw new FailedLoginException(AuthyClientInstance.getErrorMessage(verification.getError()));
        }

        return createHandlerResult(tokenCredential, principal, new ArrayList<>(0));
    }

    @SneakyThrows
    private Token verifyAuthyToken(final AuthyTokenCredential tokenCredential, final User user, final Map<String, String> options) {
        return this.instance.getAuthyTokens().verify(user.getId(), tokenCredential.getToken(), options);
    }

    @Override
    public boolean supports(final Credential credential) {
        return AuthyTokenCredential.class.isAssignableFrom(credential.getClass());
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return AuthyTokenCredential.class.isAssignableFrom(clazz);
    }
}
