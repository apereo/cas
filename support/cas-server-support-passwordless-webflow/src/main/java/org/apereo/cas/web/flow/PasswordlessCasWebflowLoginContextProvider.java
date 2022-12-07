package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;

import lombok.val;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.Optional;

/**
 * This is {@link PasswordlessCasWebflowLoginContextProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class PasswordlessCasWebflowLoginContextProvider implements CasWebflowLoginContextProvider {
    @Override
    public Optional<String> getCandidateUsername(final RequestContext context) {
        val account = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(context, PasswordlessUserAccount.class);
        return Optional.ofNullable(account).map(PasswordlessUserAccount::getUsername);
    }

    @Override
    public boolean isLoginFormUsernameInputDisabled(final RequestContext requestContext) {
        return PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, Serializable.class) == null;
    }

    @Override
    public boolean isLoginFormUsernameInputVisible(final RequestContext requestContext) {
        return PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, Serializable.class) == null;
    }
}
