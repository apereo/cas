package org.jasig.cas.adaptors.generic;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.RememberMeUsernamePasswordCredential;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

/**
 * An authentication handler that routes requests to Apache Shiro.
 * Credentials are assumed to be username and password.
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("shiroAuthenticationHandler")
public class ShiroAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private Set<String> requiredRoles = new HashSet<>();
    private Set<String> requiredPermissions = new HashSet<>();

    @Value("${shiro.authn.requiredRoles:}")
    private String requiredRolesConfig;

    @Value("${shiro.authn.requiredPermissions:}")
    private String requiredPermissionsConfig;

    /**
     * Initialize roles and permissions.
     */
    @PostConstruct
    public void init() {
        if (StringUtils.isNotBlank(this.requiredPermissionsConfig)) {
            setRequiredPermissions(org.springframework.util.StringUtils.commaDelimitedListToSet(this.requiredPermissionsConfig));
        }
        if (StringUtils.isNotBlank(this.requiredRolesConfig)) {
            setRequiredRoles(org.springframework.util.StringUtils.commaDelimitedListToSet(this.requiredRolesConfig));
        }
    }


    public void setRequiredRoles(final Set<String> requiredRoles) {
        this.requiredRoles = requiredRoles;
    }

    public void setRequiredPermissions(final Set<String> requiredPermissions) {
        this.requiredPermissions = requiredPermissions;
    }

    /**
     * Sets shiro configuration to the path of the resource
     * that points to the {@code shiro.ini} file.
     *
     * @param resource the resource
     */
    @Autowired
    public void setShiroConfiguration(@Value("${shiro.authn.config.file:classpath:shiro.ini}") final Resource resource) {
        try {
            if (resource.exists()) {
                final String location = resource.getURI().toString();
                logger.debug("Loading Shiro configuration from {}", location);

                final Factory<SecurityManager> factory = new IniSecurityManagerFactory(location);
                final SecurityManager securityManager = factory.getInstance();
                SecurityUtils.setSecurityManager(securityManager);
            } else {
                logger.debug("Shiro configuration is not defined");
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential transformedCredential)
            throws GeneralSecurityException, PreventedException {
        try {
            final RememberMeUsernamePasswordCredential credential =
                    (RememberMeUsernamePasswordCredential) transformedCredential;
            final UsernamePasswordToken token = new UsernamePasswordToken(credential.getUsername(),
                    this.getPasswordEncoder().encode(credential.getPassword()));
            token.setRememberMe(credential.isRememberMe());

            final Subject currentUser = getCurrentExecutingSubject();
            currentUser.login(token);

            checkSubjectRolesAndPermissions(currentUser);

            return createAuthenticatedSubjectResult(credential, currentUser);
        } catch (final UnknownAccountException uae) {
            throw new AccountNotFoundException(uae.getMessage());
        } catch (final IncorrectCredentialsException ice)  {
            throw new FailedLoginException(ice.getMessage());
        } catch (final LockedAccountException lae) {
            throw new AccountLockedException(lae.getMessage());
        } catch (final ExcessiveAttemptsException eae) {
            throw new AccountLockedException(eae.getMessage());
        } catch (final ExpiredCredentialsException eae) {
            throw new CredentialExpiredException(eae.getMessage());
        } catch (final DisabledAccountException eae) {
            throw new AccountDisabledException(eae.getMessage());
        } catch (final AuthenticationException ae){
            throw new FailedLoginException(ae.getMessage());
        }
    }

    /**
     * Check subject roles and permissions.
     *
     * @param currentUser the current user
     * @throws FailedLoginException the failed login exception in case roles or permissions are absent
     */
    protected void checkSubjectRolesAndPermissions(final Subject currentUser) throws FailedLoginException {
        if (this.requiredRoles != null) {
            for (final String role : this.requiredRoles) {
                if (!currentUser.hasRole(role)) {
                    throw new FailedLoginException("Required role " + role + " does not exist");
                }
            }
        }

        if (this.requiredPermissions != null) {
            for (final String perm : this.requiredPermissions) {
                if (!currentUser.isPermitted(perm)) {
                    throw new FailedLoginException("Required permission " + perm + " does not exist");
                }
            }
        }
    }

    /**
     * Create authenticated subject result.
     *
     * @param credential the credential
     * @param currentUser the current user
     * @return the handler result
     */
    protected HandlerResult createAuthenticatedSubjectResult(final Credential credential,
                                                             final Subject currentUser) {
        final String username = currentUser.getPrincipal().toString();
        return createHandlerResult(credential, this.principalFactory.createPrincipal(username), null);
    }

    /**
     * Gets current executing subject.
     *
     * @return the current executing subject
     */
    protected Subject getCurrentExecutingSubject() {
        return SecurityUtils.getSubject();
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof RememberMeUsernamePasswordCredential;
    }
}

