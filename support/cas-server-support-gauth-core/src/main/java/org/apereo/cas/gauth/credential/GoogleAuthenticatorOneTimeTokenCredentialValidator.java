package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.gauth.token.GoogleAuthenticatorToken;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialValidator;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountNotFoundException;
import java.security.GeneralSecurityException;

/**
 * This is {@link GoogleAuthenticatorOneTimeTokenCredentialValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class GoogleAuthenticatorOneTimeTokenCredentialValidator implements
    OneTimeTokenCredentialValidator<GoogleAuthenticatorTokenCredential, GoogleAuthenticatorToken> {
    private final IGoogleAuthenticator googleAuthenticatorInstance;

    private final OneTimeTokenRepository tokenRepository;

    private final OneTimeTokenCredentialRepository credentialRepository;

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
        var authzAccount = accounts.stream()
            .filter(ac -> {
                if (isCredentialAssignedToAccount(tokenCredential, ac)) {
                    return isTokenAuthorizedFor(otp, ac);
                }
                return false;
            })
            .map(GoogleAuthenticatorAccount.class::cast)
            .findFirst()
            .orElse(null);

        if (authzAccount == null) {
            authzAccount = accounts
                .stream()
                .filter(ac -> {
                    if (isCredentialAssignedToAccount(tokenCredential, ac)) {
                        return ac.getScratchCodes().contains(otp);
                    }
                    return false;
                })
                .map(GoogleAuthenticatorAccount.class::cast)
                .findFirst()
                .orElse(null);

            if (authzAccount != null) {
                LOGGER.warn("Using scratch code [{}] to authenticate user [{}]. Scratch code will be removed", otp, uid);
                authzAccount.getScratchCodes().removeIf(token -> token == otp);
                credentialRepository.update(authzAccount);
            }
        }
        return new GoogleAuthenticatorToken(otp, uid);
    }

    @Override
    public void store(final GoogleAuthenticatorToken validatedToken) {
        this.tokenRepository.store(validatedToken);
    }

    @Override
    public boolean isTokenAuthorizedFor(final int token, final OneTimeTokenAccount account) {
        return this.googleAuthenticatorInstance.authorize(account.getSecretKey(), token);
    }

    private static boolean isCredentialAssignedToAccount(final GoogleAuthenticatorTokenCredential credential,
                                                         final OneTimeTokenAccount account) {
        return credential.getAccountId() == null || credential.getAccountId() == account.getId();
    }
}
