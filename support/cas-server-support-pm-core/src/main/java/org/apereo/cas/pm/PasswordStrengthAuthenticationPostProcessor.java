package org.apereo.cas.pm;

import module java.base;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link PasswordStrengthAuthenticationPostProcessor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class PasswordStrengthAuthenticationPostProcessor implements AuthenticationPostProcessor {
    private final PasswordValidationService passwordValidationService;

    @Override
    public void process(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) throws Throwable {
        val primaryCredential = (UsernamePasswordCredential) transaction.getPrimaryCredential().orElseThrow();
        if (!passwordValidationService.isAcceptedByPasswordPolicy(primaryCredential.toPassword())) {
            throw new WeakPasswordException(primaryCredential);
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof UsernamePasswordCredential;
    }
}
