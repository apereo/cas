package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;

import lombok.val;
import org.springframework.webflow.execution.RequestContext;

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
}
