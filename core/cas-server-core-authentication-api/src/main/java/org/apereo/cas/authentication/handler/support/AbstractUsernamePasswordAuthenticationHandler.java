package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.AuthenticationPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.services.ServicesManager;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * Abstract class to override supports so that we don't need to duplicate the
 * check for UsernamePasswordCredential.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@Slf4j
@Setter
@Getter
public abstract class AbstractUsernamePasswordAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    /**
     * Decide how to execute password policy handling, if at all.
     */
    protected AuthenticationPasswordPolicyHandlingStrategy passwordPolicyHandlingStrategy = (o, o2) -> new ArrayList<>(0);

    private PasswordEncoder passwordEncoder = NoOpPasswordEncoder.getInstance();

    private PrincipalNameTransformer principalNameTransformer = formUserId -> formUserId;

    private PasswordPolicyContext passwordPolicyConfiguration;

    public AbstractUsernamePasswordAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                                         final PrincipalFactory principalFactory, final Integer order) {
        super(name, servicesManager, principalFactory, order);
    }

    @SneakyThrows
    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) {
        val originalUserPass = (UsernamePasswordCredential) credential;
        val userPass = (UsernamePasswordCredential) credential.getClass().getDeclaredConstructor().newInstance();

        BeanUtils.copyProperties(userPass, originalUserPass);

        transformUsername(userPass);
        transformPassword(userPass);

        LOGGER.debug("Attempting authentication internally for transformed credential [{}]", userPass);
        return authenticateUsernamePasswordInternal(userPass, originalUserPass.getPassword());
    }

    /**
     * Transform password.
     *
     * @param userPass the user pass
     * @throws FailedLoginException     the failed login exception
     * @throws AccountNotFoundException the account not found exception
     */
    protected void transformPassword(final UsernamePasswordCredential userPass) throws FailedLoginException, AccountNotFoundException {
        if (StringUtils.isBlank(userPass.getPassword())) {
            throw new FailedLoginException("Password is null.");
        }
        LOGGER.debug("Attempting to encode credential password via [{}] for [{}]", this.passwordEncoder.getClass().getName(), userPass.getUsername());
        val transformedPsw = this.passwordEncoder.encode(userPass.getPassword());
        if (StringUtils.isBlank(transformedPsw)) {
            throw new AccountNotFoundException("Encoded password is null.");
        }
        userPass.setPassword(transformedPsw);
    }

    /**
     * Transform username.
     *
     * @param userPass the user pass
     * @throws AccountNotFoundException the account not found exception
     */
    protected void transformUsername(final UsernamePasswordCredential userPass) throws AccountNotFoundException {
        if (StringUtils.isBlank(userPass.getUsername())) {
            throw new AccountNotFoundException("Username is null.");
        }
        LOGGER.debug("Transforming credential username via [{}]", this.principalNameTransformer.getClass().getName());
        val transformedUsername = this.principalNameTransformer.transform(userPass.getUsername());
        if (StringUtils.isBlank(transformedUsername)) {
            throw new AccountNotFoundException("Transformed username is null.");
        }
        userPass.setUsername(transformedUsername);
    }

    /**
     * Authenticates a username/password credential by an arbitrary strategy with extra parameter original credential password before
     * encoding password. Override it if implementation need to use original password for authentication.
     *
     * @param credential       the credential object bearing the transformed username and password.
     * @param originalPassword original password from credential before password encoding
     * @return AuthenticationHandlerExecutionResult resolved from credential on authentication success or null if no principal could be resolved
     * from the credential.
     * @throws GeneralSecurityException On authentication failure.
     * @throws PreventedException       On the indeterminate case when authentication is prevented.
     */
    protected abstract AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(UsernamePasswordCredential credential,
                                                                                                 String originalPassword) throws GeneralSecurityException, PreventedException;

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return UsernamePasswordCredential.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean supports(final Credential credential) {
        if (!UsernamePasswordCredential.class.isInstance(credential)) {
            LOGGER.debug("Credential is not one of username/password and is not accepted by handler [{}]", getName());
            return false;
        }
        if (this.credentialSelectionPredicate == null) {
            LOGGER.debug("No credential selection criteria is defined for handler [{}]. Credential is accepted for further processing", getName());
            return true;
        }
        LOGGER.debug("Examining credential [{}] eligibility for authentication handler [{}]", credential, getName());
        val result = this.credentialSelectionPredicate.test(credential);
        LOGGER.debug("Credential [{}] eligibility is [{}] for authentication handler [{}]", credential, getName(), BooleanUtils.toStringTrueFalse(result));
        return result;
    }

    /**
     * Used in case passwordEncoder is used to match raw password with encoded password. Mainly for BCRYPT password encoders where each encoded
     * password is different and we cannot use traditional compare of encoded strings to check if passwords match
     *
     * @param charSequence raw not encoded password
     * @param password     encoded password to compare with
     * @return true in case charSequence matched encoded password
     */
    protected boolean matches(final CharSequence charSequence, final String password) {
        return this.passwordEncoder.matches(charSequence, password);
    }
}
