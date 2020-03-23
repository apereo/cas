package org.apereo.cas.authentication.support;

import org.apereo.cas.DefaultMessageDescriptor;
import org.apereo.cas.authentication.AuthenticationAccountStateHandler;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginTimeException;
import org.apereo.cas.authentication.support.password.PasswordExpiringWarningMessageDescriptor;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.util.DateTimeUtils;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.ldaptive.auth.AccountState;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.ext.ActiveDirectoryAccountState;
import org.ldaptive.auth.ext.EDirectoryAccountState;
import org.ldaptive.auth.ext.FreeIPAAccountState;
import org.ldaptive.auth.ext.PasswordExpirationAccountState;
import org.ldaptive.control.PasswordPolicyControl;
import org.springframework.util.LinkedCaseInsensitiveMap;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default account state handler.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
public class DefaultLdapAccountStateHandler implements AuthenticationAccountStateHandler<AuthenticationResponse, PasswordPolicyContext> {
    private static final int DEFAULT_ERROR_COUNT = 30;

    /**
     * Map of account state error to CAS authentication exception.
     */
    protected Map<AccountState.Error, LoginException> errorMap;

    @Setter
    private Map<String, Class<? extends LoginException>> attributesToErrorMap = new LinkedCaseInsensitiveMap<>(DEFAULT_ERROR_COUNT);

    /**
     * Instantiates a new account state handler, that populates
     * the error map with LDAP error codes and corresponding exceptions.
     */
    public DefaultLdapAccountStateHandler() {
        this.errorMap = new HashMap<>(DEFAULT_ERROR_COUNT);
        this.errorMap.put(ActiveDirectoryAccountState.Error.ACCOUNT_DISABLED, new AccountDisabledException());
        this.errorMap.put(ActiveDirectoryAccountState.Error.ACCOUNT_LOCKED_OUT, new AccountLockedException());
        this.errorMap.put(ActiveDirectoryAccountState.Error.INVALID_LOGON_HOURS, new InvalidLoginTimeException());
        this.errorMap.put(ActiveDirectoryAccountState.Error.INVALID_WORKSTATION, new InvalidLoginLocationException());
        this.errorMap.put(ActiveDirectoryAccountState.Error.PASSWORD_MUST_CHANGE, new AccountPasswordMustChangeException());
        this.errorMap.put(ActiveDirectoryAccountState.Error.PASSWORD_EXPIRED, new CredentialExpiredException());
        this.errorMap.put(ActiveDirectoryAccountState.Error.ACCOUNT_EXPIRED, new AccountExpiredException());
        this.errorMap.put(EDirectoryAccountState.Error.ACCOUNT_EXPIRED, new AccountExpiredException());
        this.errorMap.put(EDirectoryAccountState.Error.LOGIN_LOCKOUT, new AccountLockedException());
        this.errorMap.put(EDirectoryAccountState.Error.LOGIN_TIME_LIMITED, new InvalidLoginTimeException());
        this.errorMap.put(EDirectoryAccountState.Error.PASSWORD_EXPIRED, new CredentialExpiredException());
        this.errorMap.put(PasswordExpirationAccountState.Error.PASSWORD_EXPIRED, new CredentialExpiredException());
        this.errorMap.put(PasswordPolicyControl.Error.ACCOUNT_LOCKED, new AccountLockedException());
        this.errorMap.put(PasswordPolicyControl.Error.PASSWORD_EXPIRED, new CredentialExpiredException());
        this.errorMap.put(PasswordPolicyControl.Error.CHANGE_AFTER_RESET, new AccountPasswordMustChangeException());
        this.errorMap.put(FreeIPAAccountState.Error.FAILED_AUTHENTICATION, new FailedLoginException());
        this.errorMap.put(FreeIPAAccountState.Error.PASSWORD_EXPIRED, new CredentialExpiredException());
        this.errorMap.put(FreeIPAAccountState.Error.ACCOUNT_EXPIRED, new AccountExpiredException());
        this.errorMap.put(FreeIPAAccountState.Error.MAXIMUM_LOGINS_EXCEEDED, new AccountLockedException());
        this.errorMap.put(FreeIPAAccountState.Error.LOGIN_TIME_LIMITED, new InvalidLoginTimeException());
        this.errorMap.put(FreeIPAAccountState.Error.LOGIN_LOCKOUT, new AccountLockedException());
        this.errorMap.put(FreeIPAAccountState.Error.ACCOUNT_NOT_FOUND, new AccountNotFoundException());
        this.errorMap.put(FreeIPAAccountState.Error.CREDENTIAL_NOT_FOUND, new FailedLoginException());
        this.errorMap.put(FreeIPAAccountState.Error.ACCOUNT_DISABLED, new AccountDisabledException());
    }

    @Override
    public List<MessageDescriptor> handle(final AuthenticationResponse response, final PasswordPolicyContext configuration) throws LoginException {
        LOGGER.debug("Attempting to handle LDAP account state for [{}]", response);
        if (!this.attributesToErrorMap.isEmpty() && response.isSuccess()) {
            LOGGER.debug("Handling policy based on pre-defined attributes");
            handlePolicyAttributes(response);
        }

        val state = response.getAccountState();
        if (state == null) {
            LOGGER.debug("Account state not defined. Returning empty list of messages.");
            return new ArrayList<>(0);
        }
        val messages = new ArrayList<MessageDescriptor>();
        handleError(state.getError(), response, configuration, messages);
        handleWarning(state.getWarning(), response, configuration, messages);

        return messages;
    }

    /**
     * Handle an account state error produced by ldaptive account state machinery.
     * <p>
     * Override this method to provide custom error handling.
     *
     * @param error         Account state error.
     * @param response      Ldaptive authentication response.
     * @param configuration Password policy configuration.
     * @param messages      Container for messages produced by account state error handling.
     * @throws LoginException On errors that should be communicated as login exceptions.
     */
    protected void handleError(final AccountState.Error error, final AuthenticationResponse response,
                               final PasswordPolicyContext configuration, final List<MessageDescriptor> messages) throws LoginException {

        LOGGER.debug("Handling LDAP account state error [{}]", error);
        if (errorMap.containsKey(error)) {
            throw errorMap.get(error);
        }
        LOGGER.debug("No LDAP error mapping defined for [{}]", error);
    }


    /**
     * Handle an account state warning produced by ldaptive account state machinery.
     * <p>
     * Override this method to provide custom warning message handling.
     *
     * @param warning       the account state warning messages.
     * @param response      Ldaptive authentication response.
     * @param configuration Password policy configuration.
     * @param messages      Container for messages produced by account state warning handling.
     */
    protected void handleWarning(final AccountState.Warning warning, final AuthenticationResponse response,
                                 final PasswordPolicyContext configuration, final List<MessageDescriptor> messages) {


        LOGGER.debug("Handling account state warning [{}]", warning);
        if (warning == null) {
            LOGGER.debug("Account state warning not defined");
            return;
        }

        if (warning.getExpiration() != null) {
            val expDate = DateTimeUtils.zonedDateTimeOf(warning.getExpiration());
            val ttl = ZonedDateTime.now(ZoneOffset.UTC).until(expDate, ChronoUnit.DAYS);
            LOGGER.debug(
                "Password expires in [{}] days. Expiration warning threshold is [{}] days.",
                ttl,
                configuration.getPasswordWarningNumberOfDays());
            if (configuration.isAlwaysDisplayPasswordExpirationWarning() || ttl < configuration.getPasswordWarningNumberOfDays()) {
                messages.add(new PasswordExpiringWarningMessageDescriptor("Password expires in {0} days.", ttl));
            }
        } else {
            LOGGER.debug("No account expiration warning was provided as part of the account state");
        }

        if (warning.getLoginsRemaining() > 0) {
            messages.add(new DefaultMessageDescriptor(
                "password.expiration.loginsRemaining",
                "You have {0} logins remaining before you MUST change your password.",
                new Serializable[]{warning.getLoginsRemaining()}));

        }
    }

    /**
     * Maps boolean attribute values to their corresponding exception.
     * This handles ad-hoc password policies.
     *
     * @param response the authentication response.
     */
    @SneakyThrows
    protected void handlePolicyAttributes(final AuthenticationResponse response) {
        val attributes = response.getLdapEntry().getAttributes();
        for (val attr : attributes) {
            if (this.attributesToErrorMap.containsKey(attr.getName()) && Boolean.parseBoolean(attr.getStringValue())) {
                val clazz = this.attributesToErrorMap.get(attr.getName());
                throw clazz.getDeclaredConstructor().newInstance();
            }
        }
    }
}

