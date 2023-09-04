package org.apereo.cas.adaptors.authy;

import org.apereo.cas.adaptors.authy.core.AuthyClientInstance;
import org.apereo.cas.adaptors.authy.core.AuthyTokenCredential;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;

import com.authy.api.Token;
import com.authy.api.User;
import lombok.Getter;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Authy authentication handler for CAS.
 *
 * @author Jérémie POISSON
 * 
 */
@Getter
public class AuthyAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler implements MultifactorAuthenticationHandler {

    private final boolean forceVerification;

    private final AuthyClientInstance instance;

    private final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider;

    public AuthyAuthenticationHandler(final String name,
                                      final ServicesManager servicesManager,
                                      final PrincipalFactory principalFactory,
                                      final AuthyClientInstance instance,
                                      final boolean forceVerification,
                                      final Integer order,
                                      final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider) {
        super(name, servicesManager, principalFactory, order);
        this.instance = instance;
        this.forceVerification = forceVerification;
        this.multifactorAuthenticationProvider = multifactorAuthenticationProvider;
    }

    @Override
    public boolean supports(final Credential credential) {
        return AuthyTokenCredential.class.isAssignableFrom(credential.getClass());
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return AuthyTokenCredential.class.isAssignableFrom(clazz);
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential, final Service service) throws GeneralSecurityException {
        val tokenCredential = (AuthyTokenCredential) credential;

        val authentication = Objects.requireNonNull(WebUtils.getInProgressAuthentication(),
            "CAS has no reference to an authentication event to locate a principal");
        val principal = authentication.getPrincipal();

        val user = Unchecked.supplier(() -> instance.getOrCreateUser(principal)).get();
        if (!user.isOk()) {
            throw new FailedLoginException(AuthyClientInstance.getErrorMessage(user.getError()));
        }

        val options = new HashMap<String, String>(1);
        options.put("force", Boolean.toString(this.forceVerification));

        val verification = Unchecked.supplier(() -> verifyAuthyToken(tokenCredential, user, options)).get();
        if (!verification.isOk()) {
            throw new FailedLoginException(AuthyClientInstance.getErrorMessage(verification.getError()));
        }

        return createHandlerResult(tokenCredential, principal, new ArrayList<>(0));
    }

    private Token verifyAuthyToken(final AuthyTokenCredential tokenCredential, final User user,
                                   final Map<String, String> options) throws Exception {
        return instance.getAuthyClient().getTokens().verify(user.getId(), tokenCredential.getToken(), options);
    }
}
