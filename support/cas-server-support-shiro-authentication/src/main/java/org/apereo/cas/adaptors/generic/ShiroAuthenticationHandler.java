package org.apereo.cas.adaptors.generic;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.ResourceUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.subject.Subject;
import org.springframework.core.io.Resource;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * An authentication handler that routes requests to Apache Shiro.
 * Credentials are assumed to be username and password.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public class ShiroAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private final Set<String> requiredRoles;

    private final Set<String> requiredPermissions;

    public ShiroAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                      final Set<String> requiredRoles, final Set<String> requiredPermissions) {
        super(name, servicesManager, principalFactory, null);
        this.requiredRoles = requiredRoles;
        this.requiredPermissions = requiredPermissions;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential transformedCredential,
                                                                                        final String originalPassword)
        throws GeneralSecurityException {
        try {
            val token = new UsernamePasswordToken(transformedCredential.getUsername(),
                transformedCredential.getPassword());

            if (transformedCredential instanceof RememberMeUsernamePasswordCredential) {
                token.setRememberMe(RememberMeUsernamePasswordCredential.class.cast(transformedCredential).isRememberMe());
            }

            val currentUser = getCurrentExecutingSubject();
            currentUser.login(token);

            checkSubjectRolesAndPermissions(currentUser);

            val strategy = getPasswordPolicyHandlingStrategy();
            val messageList = new ArrayList<MessageDescriptor>();
            if (strategy != null) {
                LOGGER.debug("Attempting to examine and handle password policy via [{}]", strategy.getClass().getSimpleName());
                val principal = this.principalFactory.createPrincipal(token.getUsername());
                messageList.addAll(strategy.handle(principal, getPasswordPolicyConfiguration()));
            }
            return createAuthenticatedSubjectResult(transformedCredential, currentUser, messageList);
        } catch (final UnknownAccountException uae) {
            throw new AccountNotFoundException(uae.getMessage());
        } catch (final LockedAccountException | ExcessiveAttemptsException lae) {
            throw new AccountLockedException(lae.getMessage());
        } catch (final ExpiredCredentialsException eae) {
            throw new CredentialExpiredException(eae.getMessage());
        } catch (final DisabledAccountException eae) {
            throw new AccountDisabledException(eae.getMessage());
        } catch (final AuthenticationException ice) {
            throw new FailedLoginException(ice.getMessage());
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
            for (val role : this.requiredRoles) {
                if (!currentUser.hasRole(role)) {
                    throw new FailedLoginException("Required role " + role + " does not exist");
                }
            }
        }

        if (this.requiredPermissions != null) {
            for (val perm : this.requiredPermissions) {
                if (!currentUser.isPermitted(perm)) {
                    throw new FailedLoginException("Required permission " + perm + " cannot be located");
                }
            }
        }
    }

    /**
     * Create authenticated subject result.
     *
     * @param credential  the credential
     * @param currentUser the current user
     * @param messages    the messages
     * @return the handler result
     */
    protected AuthenticationHandlerExecutionResult createAuthenticatedSubjectResult(final Credential credential,
                                                                                    final Subject currentUser,
                                                                                    final List<MessageDescriptor> messages) {
        val username = currentUser.getPrincipal().toString();
        return createHandlerResult(credential, this.principalFactory.createPrincipal(username), messages);
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
    @SneakyThrows
    public void loadShiroConfiguration(final Resource resource) {
        val shiroResource = ResourceUtils.prepareClasspathResourceIfNeeded(resource);
        if (shiroResource != null && shiroResource.exists()) {
            val location = shiroResource.getURI().toString();
            LOGGER.debug("Loading Shiro configuration from [{}]", location);
            val factory = new IniSecurityManagerFactory(location);
            val securityManager = factory.getInstance();
            SecurityUtils.setSecurityManager(securityManager);
        } else {
            LOGGER.debug("Shiro configuration is not defined");
        }
    }
}

