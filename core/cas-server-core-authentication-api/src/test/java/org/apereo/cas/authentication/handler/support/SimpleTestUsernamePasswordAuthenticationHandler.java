package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginTimeException;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple test implementation of a AuthenticationHandler that returns true if
 * the username and password match. This class should never be enabled in a
 * production environment and is only designed to facilitate unit testing and
 * load testing.
 *
 * @author Scott Battagliaa
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@Slf4j
public class SimpleTestUsernamePasswordAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    /**
     * Default mapping of special usernames to exceptions raised when that user attempts authentication.
     */
    private static final Map<String, Exception> DEFAULT_USERNAME_ERROR_MAP = new HashMap<>();

    static {
        DEFAULT_USERNAME_ERROR_MAP.put("accountDisabled", new AccountDisabledException("Account disabled"));
        DEFAULT_USERNAME_ERROR_MAP.put("accountLocked", new AccountLockedException("Account locked"));
        DEFAULT_USERNAME_ERROR_MAP.put("badHours", new InvalidLoginTimeException("Invalid logon hours"));
        DEFAULT_USERNAME_ERROR_MAP.put("badWorkstation", new InvalidLoginLocationException("Invalid workstation"));
        DEFAULT_USERNAME_ERROR_MAP.put("passwordExpired", new CredentialExpiredException("Password expired"));
    }

    /**
     * Map of special usernames to exceptions that are raised when a user with that name attempts authentication.
     */
    private final Map<String, Exception> usernameErrorMap = DEFAULT_USERNAME_ERROR_MAP;

    private final List<MessageDescriptor> warnings = new ArrayList<>();
    private final Map<String, List<Object>> userAttributes = new HashMap<>();

    public SimpleTestUsernamePasswordAuthenticationHandler() {
        this(StringUtils.EMPTY);
    }

    public SimpleTestUsernamePasswordAuthenticationHandler(final String name) {
        super(name, PrincipalFactoryUtils.newPrincipalFactory(), null);
    }

    public void addMessageDescriptor(final MessageDescriptor msg) {
        this.warnings.add(msg);
    }

    public SimpleTestUsernamePasswordAuthenticationHandler putAttribute(final String name, final List<Object> values) {
        this.userAttributes.put(name, values);
        return this;
    }

    public SimpleTestUsernamePasswordAuthenticationHandler putAttributes(final Map<String, List<Object>> attributes) {
        this.userAttributes.putAll(attributes);
        return this;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(
        final UsernamePasswordCredential credential, final String originalPassword) throws Throwable {

        val username = credential.getUsername();
        val password = credential.toPassword();

        val exception = this.usernameErrorMap.get(username);
        if (exception instanceof final GeneralSecurityException gse) {
            throw gse;
        }
        if (exception instanceof final PreventedException pe) {
            throw pe;
        }
        if (exception instanceof final RuntimeException re) {
            throw re;
        }
        if (exception != null) {
            LOGGER.debug("Cannot throw checked exception [{}] since it is not declared by method signature.",
                exception.getClass().getName(), exception);
        }

        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)
            && (username.equals(password) || password.equals(StringUtils.reverse(username)))) {
            LOGGER.debug("User [{}] was successfully authenticated.", username);
            return new DefaultAuthenticationHandlerExecutionResult(this,
                credential, principalFactory.createPrincipal(username, this.userAttributes), this.warnings);
        }
        LOGGER.debug("User [{}] failed authentication", username);
        throw new FailedLoginException();
    }
}
