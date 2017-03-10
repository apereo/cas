package org.apereo.cas.authentication.handler.support;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.function.Predicate;

/**
 * Abstract class to override supports so that we don't need to duplicate the
 * check for UsernamePasswordCredential.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public abstract class AbstractUsernamePasswordAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractUsernamePasswordAuthenticationHandler.class);
    
    private PasswordEncoder passwordEncoder = NoOpPasswordEncoder.getInstance();

    private PrincipalNameTransformer principalNameTransformer = formUserId -> formUserId;

    private Predicate<Credential> credentialSelectionPredicate = credential -> true;

    private PasswordPolicyConfiguration passwordPolicyConfiguration;

    public AbstractUsernamePasswordAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                                         final Integer order) {
        super(name, servicesManager, principalFactory, order);
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {

        final UsernamePasswordCredential originalUserPass = (UsernamePasswordCredential) credential;
        final UsernamePasswordCredential userPass = new UsernamePasswordCredential(originalUserPass.getUsername(), originalUserPass.getPassword());

        if (StringUtils.isBlank(userPass.getUsername())) {
            throw new AccountNotFoundException("Username is null.");
        }

        LOGGER.debug("Transforming credential username via [{}]", this.principalNameTransformer.getClass().getName());
        final String transformedUsername = this.principalNameTransformer.transform(userPass.getUsername());
        if (StringUtils.isBlank(transformedUsername)) {
            throw new AccountNotFoundException("Transformed username is null.");
        }

        if (StringUtils.isBlank(userPass.getPassword())) {
            throw new FailedLoginException("Password is null.");
        }

        LOGGER.debug("Attempting to encode credential password via [{}] for [{}]", this.passwordEncoder.getClass().getName(), transformedUsername);
        final String transformedPsw = this.passwordEncoder.encode(userPass.getPassword());
        if (StringUtils.isBlank(transformedPsw)) {
            throw new AccountNotFoundException("Encoded password is null.");
        }

        userPass.setUsername(transformedUsername);
        userPass.setPassword(transformedPsw);
        
        LOGGER.debug("Attempting authentication internally for transformed credential [{}]", userPass);
        return authenticateUsernamePasswordInternal(userPass, originalUserPass.getPassword());
    }


    /**
     * Authenticates a username/password credential by an arbitrary strategy with extra parameter original credential password before
     * encoding password. Override it if implementation need to use original password for authentication.
     *
     * @param transformedCredential the credential object bearing the transformed username and password.
     * @param originalPassword      original password from credential before password encoding
     * @return HandlerResult resolved from credential on authentication success or null if no principal could be resolved
     * from the credential.
     * @throws GeneralSecurityException On authentication failure.
     * @throws PreventedException       On the indeterminate case when authentication is prevented.
     */
    protected abstract HandlerResult authenticateUsernamePasswordInternal(UsernamePasswordCredential transformedCredential, String originalPassword) 
            throws GeneralSecurityException, PreventedException;

    protected PasswordPolicyConfiguration getPasswordPolicyConfiguration() {
        return this.passwordPolicyConfiguration;
    }

    public void setPasswordEncoder(final PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void setCredentialSelectionPredicate(final Predicate<Credential> credentialSelectionPredicate) {
        this.credentialSelectionPredicate = credentialSelectionPredicate;
    }

    public void setPrincipalNameTransformer(final PrincipalNameTransformer principalNameTransformer) {
        this.principalNameTransformer = principalNameTransformer;
    }

    public void setPasswordPolicyConfiguration(final PasswordPolicyConfiguration passwordPolicyConfiguration) {
        this.passwordPolicyConfiguration = passwordPolicyConfiguration;
    }

    @Override
    public boolean supports(final Credential credential) {
        if (credential instanceof UsernamePasswordCredential) {
            if (this.credentialSelectionPredicate != null) {
                return this.credentialSelectionPredicate.test(credential);
            }
            return true;
        }
        return false;
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
