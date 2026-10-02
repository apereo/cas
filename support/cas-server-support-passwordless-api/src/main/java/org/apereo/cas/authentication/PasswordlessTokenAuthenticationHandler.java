package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link PasswordlessTokenAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class PasswordlessTokenAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    private final PasswordlessTokenRepository passwordlessTokenRepository;

    public PasswordlessTokenAuthenticationHandler(final String name,
                                                  final PrincipalFactory principalFactory, final Integer order,
                                                  final PasswordlessTokenRepository passwordlessTokenRepository) {
        super(name, principalFactory, order);
        this.passwordlessTokenRepository = passwordlessTokenRepository;
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential, final Service service) throws Throwable {
        val otc = (PasswordlessTokenCredential) credential;
        val passed = passwordlessTokenRepository.findToken(otc.getId())
            .filter(token -> StringUtils.isNotBlank(token.getToken()) && token.getToken().equalsIgnoreCase(otc.getPassword()))
            .isPresent();
        if (passed) {
            val principal = principalFactory.createPrincipal(otc.getId());
            return createHandlerResult(credential, principal, new ArrayList<>());
        }
        throw new FailedLoginException("Passwordless authentication has failed");
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return PasswordlessTokenCredential.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean supports(final Credential credential) {
        if (!(credential instanceof PasswordlessTokenCredential)) {
            LOGGER.debug("Credential is not a passwordless token credential and is not accepted by handler [{}]", getName());
            return false;
        }
        return true;
    }
}
