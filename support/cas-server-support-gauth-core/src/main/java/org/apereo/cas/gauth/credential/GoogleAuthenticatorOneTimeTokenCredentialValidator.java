package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.gauth.token.GoogleAuthenticatorToken;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialValidator;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountNotFoundException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Optional;

/**
 * This is {@link GoogleAuthenticatorOneTimeTokenCredentialValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class GoogleAuthenticatorOneTimeTokenCredentialValidator implements
    OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> {
    private final IGoogleAuthenticator googleAuthenticatorInstance;

    private final OneTimeTokenRepository tokenRepository;

    private final OneTimeTokenCredentialRepository credentialRepository;

    private static boolean isCredentialAssignedToAccount(final GoogleAuthenticatorTokenCredential credential,
                                                         final OneTimeTokenAccount account) {
        return credential.getAccountId() == null || credential.getAccountId() == account.getId();
    }

    @Override
    public GoogleAuthenticatorToken validate(final Authentication authentication,
                                             final GoogleAuthenticatorTokenCredential tokenCredential)
        throws GeneralSecurityException, PreventedException {

        if (!StringUtils.isNumeric(tokenCredential.getToken())) {
            throw new PreventedException("Invalid non-numeric OTP format specified.");
        }

        val uid = authentication.getPrincipal().getId();
        val otp = Integer.parseInt(tokenCredential.getToken());
        LOGGER.trace("Received OTP [{}] assigned to account [{}]", otp, tokenCredential.getAccountId());

        LOGGER.trace("Received principal id [{}]. Attempting to locate account in credential repository...", uid);
        val accounts = this.credentialRepository.get(uid);
        if (accounts == null || accounts.isEmpty()) {
            throw new AccountNotFoundException(uid + " cannot be found in the registry");
        }

        if (accounts.size() > 1 && tokenCredential.getAccountId() == null) {
            throw new PreventedException("Account identifier must be specified if multiple accounts are registered for " + uid);
        }
        LOGGER.trace("Attempting to locate OTP token [{}] in token repository for [{}]...", otp, uid);
        if (this.tokenRepository.exists(uid, otp)) {
            throw new AccountExpiredException(uid + " cannot reuse OTP " + otp + " as it may be expired/invalid");
        }

        LOGGER.debug("Attempting to authorize OTP token [{}]...", otp);
        val result = getAuthorizedAccountForToken(tokenCredential, accounts)
            .or(() -> getAuthorizedScratchCodeForToken(tokenCredential, authentication, accounts));
        return result
            .map(acct -> new GoogleAuthenticatorToken(otp, uid))
            .orElse(null);
    }

    @Override
    public OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> store(final GoogleAuthenticatorToken validatedToken) {
        this.tokenRepository.store(validatedToken);
        return this;
    }

    @Override
    public boolean isTokenAuthorizedFor(final int token, final OneTimeTokenAccount account) {
        return this.googleAuthenticatorInstance.authorize(account.getSecretKey(), token);
    }

    /**
     * Gets authorized scratch code for token.
     *
     * @param tokenCredential the token credential
     * @param authentication  the authentication
     * @param accounts        the accounts
     * @return the authorized scratch code for token
     */
    protected Optional<GoogleAuthenticatorAccount> getAuthorizedScratchCodeForToken(final GoogleAuthenticatorTokenCredential tokenCredential,
                                                                                    final Authentication authentication,
                                                                                    final Collection<? extends OneTimeTokenAccount> accounts) {
        val uid = authentication.getPrincipal().getId();
        val otp = Integer.parseInt(tokenCredential.getToken());
        return accounts
            .stream()
            .filter(ac -> isCredentialAssignedToAccount(tokenCredential, ac) && ac.getScratchCodes().contains(otp))
            .map(GoogleAuthenticatorAccount.class::cast)
            .peek(acct -> {
                LOGGER.info("Using scratch code [{}] to authenticate user [{}]. Scratch code will be removed", otp, uid);
                acct.getScratchCodes().removeIf(token -> token == otp);
                credentialRepository.update(acct);
            })
            .findFirst();
    }

    /**
     * Gets authorized account for token.
     *
     * @param tokenCredential the token credential
     * @param accounts        the accounts
     * @return the authorized account for token
     */
    protected Optional<GoogleAuthenticatorAccount> getAuthorizedAccountForToken(final GoogleAuthenticatorTokenCredential tokenCredential,
                                                                                final Collection<? extends OneTimeTokenAccount> accounts) {
        val otp = Integer.parseInt(tokenCredential.getToken());
        return accounts.stream()
            .filter(ac -> isCredentialAssignedToAccount(tokenCredential, ac) && isTokenAuthorizedFor(otp, ac))
            .map(GoogleAuthenticatorAccount.class::cast)
            .findFirst();
    }
}
