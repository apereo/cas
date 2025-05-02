package org.apereo.cas.okta;

import org.apereo.cas.authentication.AuthenticationPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;

import com.okta.authn.sdk.AuthenticationStateHandlerAdapter;
import com.okta.authn.sdk.resource.AuthenticationResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link OktaAuthenticationStateHandlerAdapter}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
@Slf4j
@Getter
public class OktaAuthenticationStateHandlerAdapter extends AuthenticationStateHandlerAdapter {
    private final AuthenticationPasswordPolicyHandlingStrategy passwordPolicyHandlingStrategy;

    private final PasswordPolicyContext passwordPolicyConfiguration;

    private final Map<String, List<Object>> userAttributes = new HashMap<>();

    private String username;

    private Exception failureException;

    private List<MessageDescriptor> warnings = new ArrayList<>();

    @Override
    public void handleUnknown(final AuthenticationResponse authenticationResponse) {
        failureException = new AccountNotFoundException(authenticationResponse.getStatusString());
    }

    @Override
    public void handleUnauthenticated(final AuthenticationResponse unauthenticatedResponse) {
        failureException = new FailedLoginException(unauthenticatedResponse.getStatusString());
    }

    @Override
    public void handleSuccess(final AuthenticationResponse successResponse) {
        if (StringUtils.isNotBlank(successResponse.getSessionToken())) {
            val user = successResponse.getUser();
            this.username = user.getLogin();

            FunctionUtils.doIfNotNull(successResponse.getSessionToken(), value -> userAttributes.put("oktaSessionToken", CollectionUtils.wrapList(value)));
            FunctionUtils.doIfNotNull(successResponse.getStatusString(), value -> userAttributes.put("oktaStatus", CollectionUtils.wrapList(value)));
            FunctionUtils.doIfNotNull(successResponse.getType(), value -> userAttributes.put("oktaType", CollectionUtils.wrapList(value)));
            FunctionUtils.doIfNotNull(successResponse.getExpiresAt(), value -> userAttributes.put("oktaExpiration", CollectionUtils.wrapList(value)));
            FunctionUtils.doIfNotNull(successResponse.getRecoveryType(), value -> userAttributes.put("oktaRecoveryType", CollectionUtils.wrapList(value)));
            
            user.getProfile().forEach((key, value) -> userAttributes.put(key, CollectionUtils.wrapList(value)));
        } else {
            handleUnauthenticated(successResponse);
        }
    }

    @Override
    public void handlePasswordWarning(final AuthenticationResponse passwordWarning) {
        try {
            if (passwordPolicyHandlingStrategy.supports(passwordWarning)) {
                warnings = passwordPolicyHandlingStrategy.handle(passwordWarning, passwordPolicyConfiguration);
            }
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
        }
        handleUnknown(passwordWarning);
    }

    @Override
    public void handlePasswordExpired(final AuthenticationResponse passwordExpired) {
        failureException = new AccountExpiredException(passwordExpired.getStatusString());
    }

    @Override
    public void handlePasswordReset(final AuthenticationResponse passwordReset) {
        failureException = new AccountPasswordMustChangeException(passwordReset.getStatusString());
    }

    @Override
    public void handleLockedOut(final AuthenticationResponse lockedOut) {
        failureException = new AccountLockedException(lockedOut.getStatusString());
    }

    /**
     * Throw exception if necessary.
     *
     * @throws Exception the exception
     */
    public void throwExceptionIfNecessary() throws Exception {
        if (failureException != null) {
            throw this.failureException;
        }
    }
}
