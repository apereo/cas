package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.NoOpPrincipalNameTransformer;
import org.apereo.cas.authentication.handler.PasswordEncoder;
import org.apereo.cas.authentication.handler.PlainTextPasswordEncoder;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.support.PasswordPolicyConfiguration;

import javax.security.auth.login.AccountNotFoundException;
import java.security.GeneralSecurityException;

/**
 * Abstract class to override supports so that we don't need to duplicate the
 * check for UsernamePasswordCredential.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public abstract class AbstractUsernamePasswordAuthenticationHandler extends
    AbstractPreAndPostProcessingAuthenticationHandler {

    /**
     * PasswordEncoder to be used by subclasses to encode passwords for
     * comparing against a resource.
     */
    private PasswordEncoder passwordEncoder = new PlainTextPasswordEncoder();
    
    private PrincipalNameTransformer principalNameTransformer = new NoOpPrincipalNameTransformer();

    /** The password policy configuration to be used by extensions. */
    private PasswordPolicyConfiguration passwordPolicyConfiguration;

    @Override
    protected HandlerResult doAuthentication(final Credential credential)
            throws GeneralSecurityException, PreventedException {
        final UsernamePasswordCredential userPass = (UsernamePasswordCredential) credential;
        if (userPass.getUsername() == null) {
            throw new AccountNotFoundException("Username is null.");
        }
        
        final String transformedUsername = this.principalNameTransformer.transform(userPass.getUsername());
        if (transformedUsername == null) {
            throw new AccountNotFoundException("Transformed username is null.");
        }
        userPass.setUsername(transformedUsername);
        return authenticateUsernamePasswordInternal(userPass);
    }

    /**
     * Authenticates a username/password credential by an arbitrary strategy.
     *
     * @param transformedCredential the credential object bearing the transformed username and password.
     *
     * @return HandlerResult resolved from credential on authentication success or null if no principal could be resolved
     * from the credential.
     *
     * @throws GeneralSecurityException On authentication failure.
     * @throws PreventedException On the indeterminate case when authentication is prevented.
     */
    protected abstract HandlerResult authenticateUsernamePasswordInternal(UsernamePasswordCredential transformedCredential)
            throws GeneralSecurityException, PreventedException;

    /**
     * Method to return the PasswordEncoder to be used to encode passwords.
     *
     * @return the PasswordEncoder associated with this class.
     */
    protected PasswordEncoder getPasswordEncoder() {
        return this.passwordEncoder;
    }

    protected PrincipalNameTransformer getPrincipalNameTransformer() {
        return this.principalNameTransformer;
    }
    
    protected PasswordPolicyConfiguration getPasswordPolicyConfiguration() {
        return this.passwordPolicyConfiguration;
    }

    public void setPasswordEncoder(final PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void setPrincipalNameTransformer(final PrincipalNameTransformer principalNameTransformer) {
        this.principalNameTransformer = principalNameTransformer;
    }

    public void setPasswordPolicyConfiguration(final PasswordPolicyConfiguration passwordPolicyConfiguration) {
        this.passwordPolicyConfiguration = passwordPolicyConfiguration;
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof UsernamePasswordCredential;
    }
}
