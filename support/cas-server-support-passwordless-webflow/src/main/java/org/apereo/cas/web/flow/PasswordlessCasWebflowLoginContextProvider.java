package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PasswordlessCasWebflowLoginContextProvider}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class PasswordlessCasWebflowLoginContextProvider implements CasWebflowLoginContextProvider {
    @Override
    public Optional<String> getCandidateUsername(final RequestContext context) {
        val passwordlessRequest = PasswordlessWebflowUtils.getPasswordlessAuthenticationRequest(context, PasswordlessAuthenticationRequest.class);
        return Optional.ofNullable(passwordlessRequest)
            .map(PasswordlessAuthenticationRequest::getProvidedUsername)
            .filter(StringUtils::isNotBlank)
            .or(() -> {
                val account = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(context, PasswordlessUserAccount.class);
                return Optional.ofNullable(account).map(PasswordlessUserAccount::getUsername);
            });
    }

    @Override
    public boolean isLoginFormUsernameInputDisabled(final RequestContext requestContext) {
        return PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, Serializable.class) != null;
    }

    @Override
    public boolean isLoginFormUsernameInputVisible(final RequestContext requestContext) {
        return PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, Serializable.class) == null;
    }
}
