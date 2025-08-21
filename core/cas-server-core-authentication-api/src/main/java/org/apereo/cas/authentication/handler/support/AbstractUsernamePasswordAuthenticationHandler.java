package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.AuthenticationPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

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
    protected AuthenticationPasswordPolicyHandlingStrategy passwordPolicyHandlingStrategy = (o, o2) -> new ArrayList<>();

    private PasswordEncoder passwordEncoder = NoOpPasswordEncoder.getInstance();

    private PrincipalNameTransformer principalNameTransformer = String::trim;

    private PasswordPolicyContext passwordPolicyConfiguration;

    protected AbstractUsernamePasswordAuthenticationHandler(final String name, final PrincipalFactory principalFactory, final Integer order) {
        super(name, principalFactory, order);
    }

    @Override
    public boolean supports(final Credential credential) {
        if (!(credential instanceof UsernamePasswordCredential)) {
            LOGGER.debug("Credential is not one of username/password and is not accepted by handler [{}]", getName());
            return false;
        }
        if (getCredentialSelectionPredicate() == null) {
            LOGGER.debug("No credential selection criteria is defined for handler [{}]. Credential is accepted for further processing", getName());
            return true;
        }
        LOGGER.debug("Examining credential [{}] eligibility for authentication handler [{}]", credential, getName());
        val result = getCredentialSelectionPredicate().test(credential);
        LOGGER.debug("Credential [{}] eligibility is [{}] for authentication handler [{}]", credential, getName(), BooleanUtils.toStringTrueFalse(result));
        return result;
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return UsernamePasswordCredential.class.isAssignableFrom(clazz);
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential, final Service service) throws Throwable {
        val originalUserPass = (UsernamePasswordCredential) credential;
        val userPass = new UsernamePasswordCredential();
        FunctionUtils.doUnchecked(__ -> BeanUtils.copyProperties(userPass, originalUserPass));
        transformUsername(userPass);
        transformPassword(userPass);
        LOGGER.debug("Attempting authentication internally for transformed credential [{}]", userPass);
        return authenticateUsernamePasswordInternal(userPass, originalUserPass.toPassword());
    }

    protected void transformPassword(final UsernamePasswordCredential userPass) throws FailedLoginException, AccountNotFoundException {
        if (StringUtils.isBlank(userPass.toPassword())) {
            throw new FailedLoginException("Password is null.");
        }
        LOGGER.debug("Attempting to encode credential password via [{}] for [{}]", passwordEncoder.getClass().getName(), userPass.getUsername());
        val transformedPsw = passwordEncoder.encode(userPass.toPassword());
        if (StringUtils.isBlank(transformedPsw)) {
            throw new AccountNotFoundException("Encoded password is null.");
        }
        userPass.assignPassword(transformedPsw);
    }

    protected void transformUsername(final UsernamePasswordCredential userPass) throws Throwable {
        if (StringUtils.isBlank(userPass.getUsername())) {
            throw new AccountNotFoundException("Username is null.");
        }
        LOGGER.debug("Transforming credential username via [{}]", principalNameTransformer.getClass().getName());
        val transformedUsername = principalNameTransformer.transform(userPass.getUsername());
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
     * @return AuthenticationHandlerExecutionResult resolved from credential on authentication success or null if no principal could be resolved from the credential.
     * @throws Throwable the throwable
     */
    protected abstract AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(
        UsernamePasswordCredential credential,
        String originalPassword) throws Throwable;

    /**
     * Used in case passwordEncoder is used to match raw password with encoded password. Mainly for BCRYPT password encoders where each encoded
     * password is different and we cannot use traditional compare of encoded strings to check if passwords match
     *
     * @param charSequence raw not encoded password
     * @param password     encoded password to compare with
     * @return true in case charSequence matched encoded password
     */
    protected boolean matches(final CharSequence charSequence, final String password) {
        return passwordEncoder.matches(charSequence, password);
    }
}
