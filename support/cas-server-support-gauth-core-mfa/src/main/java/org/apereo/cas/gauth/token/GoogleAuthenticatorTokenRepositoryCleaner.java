package org.apereo.cas.gauth.token;

import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepositoryCleaner;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * This is {@link GoogleAuthenticatorTokenRepositoryCleaner}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class GoogleAuthenticatorTokenRepositoryCleaner extends OneTimeTokenRepositoryCleaner {

    public GoogleAuthenticatorTokenRepositoryCleaner(final OneTimeTokenRepository tokenRepository) {
        super(tokenRepository);
    }

    @Scheduled(initialDelayString = "${cas.authn.mfa.gauth.cleaner.schedule.start-delay:PT30S}",
        fixedDelayString = "${cas.authn.mfa.gauth.cleaner.schedule.repeat-interval:PT35S}")
    @Override
    public void clean() {
        super.clean();
    }
}
