package org.apereo.cas.adaptors.generic;

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
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.RememberMeUsernamePasswordCredential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.Set;

/**
 * An authentication handler that routes requests to Apache Shiro.
 * Credentials are assumed to be username and password.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class ShiroAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShiroAuthenticationHandler.class);
    
    private final Set<String> requiredRoles;
    private final Set<String> requiredPermissions;

    public ShiroAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                      final Set<String> requiredRoles, final Set<String> requiredPermissions) {
        super(name, servicesManager, principalFactory, null);
        this.requiredRoles = requiredRoles;
        this.requiredPermissions = requiredPermissions;
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential transformedCredential,
                                                                 final String originalPassword)
            throws GeneralSecurityException {
        try {
            final UsernamePasswordToken token = new UsernamePasswordToken(transformedCredential.getUsername(),
                    transformedCredential.getPassword());

            if (transformedCredential instanceof RememberMeUsernamePasswordCredential) {
                token.setRememberMe(RememberMeUsernamePasswordCredential.class.cast(transformedCredential).isRememberMe());
            }

            final Subject currentUser = getCurrentExecutingSubject();
            currentUser.login(token);

            checkSubjectRolesAndPermissions(currentUser);

            return createAuthenticatedSubjectResult(transformedCredential, currentUser);
        } catch (final UnknownAccountException uae) {
            throw new AccountNotFoundException(uae.getMessage());
        } catch (final IncorrectCredentialsException ice) {
            throw new FailedLoginException(ice.getMessage());
        } catch (final LockedAccountException|ExcessiveAttemptsException lae) {
            throw new AccountLockedException(lae.getMessage());
        } catch (final ExpiredCredentialsException eae) {
            throw new CredentialExpiredException(eae.getMessage());
        } catch (final DisabledAccountException eae) {
            throw new AccountDisabledException(eae.getMessage());
        } catch (final AuthenticationException e){
            throw new FailedLoginException(e.getMessage());
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
                    throw new FailedLoginException("Required permission " + perm + " cannot be located");
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
    protected HandlerResult createAuthenticatedSubjectResult(final Credential credential, final Subject currentUser) {
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
    
    /**
     * Sets shiro configuration to the path of the resource
     * that points to the {@code shiro.ini} file.
     *
     * @param resource the resource
     */
    public void loadShiroConfiguration(final Resource resource) {
        try {
            final Resource shiroResource = ResourceUtils.prepareClasspathResourceIfNeeded(resource);
            if (shiroResource != null && shiroResource.exists()) {
                final String location = shiroResource.getURI().toString();
                LOGGER.debug("Loading Shiro configuration from [{}]", location);

                final Factory<SecurityManager> factory = new IniSecurityManagerFactory(location);
                final SecurityManager securityManager = factory.getInstance();
                SecurityUtils.setSecurityManager(securityManager);
            } else {
                LOGGER.debug("Shiro configuration is not defined");
            }
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}

