package org.jasig.cas.authentication.handler.support;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.InvalidLoginLocationException;
import org.jasig.cas.authentication.InvalidLoginTimeException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.DefaultPrincipalFactory;
import org.jasig.cas.authentication.principal.PrincipalFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
@Component("simpleTestUsernamePasswordAuthenticationHandler")
public final class SimpleTestUsernamePasswordAuthenticationHandler implements AuthenticationHandler {
    /** Default mapping of special usernames to exceptions raised when that user attempts authentication. */
    private static final Map<String, Exception> DEFAULT_USERNAME_ERROR_MAP = new HashMap<>();

    @NotNull
    protected PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    /** Instance of logging for subclasses. */
    private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Map of special usernames to exceptions that are raised when a user with that name attempts authentication. */
    private Map<String, Exception> usernameErrorMap = DEFAULT_USERNAME_ERROR_MAP;


    static {
        DEFAULT_USERNAME_ERROR_MAP.put("accountDisabled", new AccountDisabledException("Account disabled"));
        DEFAULT_USERNAME_ERROR_MAP.put("accountLocked", new AccountLockedException("Account locked"));
        DEFAULT_USERNAME_ERROR_MAP.put("badHours", new InvalidLoginTimeException("Invalid logon hours"));
        DEFAULT_USERNAME_ERROR_MAP.put("badWorkstation", new InvalidLoginLocationException("Invalid workstation"));
        DEFAULT_USERNAME_ERROR_MAP.put("passwordExpired", new CredentialExpiredException("Password expired"));
    }

    public SimpleTestUsernamePasswordAuthenticationHandler() {}

    @PostConstruct
    private void init() {
        logger.warn("{} is only to be used in a testing environment. NEVER enable this in a production environment.",
                this.getClass().getName());
    }

    public void setUsernameErrorMap(final Map<String, Exception> map) {
        this.usernameErrorMap = map;
    }

    @Override
    public HandlerResult authenticate(final Credential credential)
            throws GeneralSecurityException, PreventedException {

        final UsernamePasswordCredential usernamePasswordCredential = (UsernamePasswordCredential) credential;
        final String username = usernamePasswordCredential.getUsername();
        final String password = usernamePasswordCredential.getPassword();

        final Exception exception = this.usernameErrorMap.get(username);
        if (exception instanceof GeneralSecurityException) {
            throw (GeneralSecurityException) exception;
        } else if (exception instanceof PreventedException) {
            throw (PreventedException) exception;
        } else if (exception instanceof RuntimeException) {
            throw (RuntimeException) exception;
        } else if (exception != null) {
            logger.debug("Cannot throw checked exception {} since it is not declared by method signature.", exception);
        }

        if (StringUtils.hasText(username) && StringUtils.hasText(password) && username.equals(password)) {
            logger.debug("User [{}] was successfully authenticated.", username);
            return new DefaultHandlerResult(this, new BasicCredentialMetaData(credential),
                    this.principalFactory.createPrincipal(username));
        }
        logger.debug("User [{}] failed authentication", username);
        throw new FailedLoginException();
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof UsernamePasswordCredential;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
}
