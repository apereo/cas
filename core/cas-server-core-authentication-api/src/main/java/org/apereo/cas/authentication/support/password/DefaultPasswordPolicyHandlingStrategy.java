package org.apereo.cas.authentication.support.password;

import module java.base;
import org.apereo.cas.authentication.AuthenticationPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.MessageDescriptor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link DefaultPasswordPolicyHandlingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class DefaultPasswordPolicyHandlingStrategy<AuthnResponse> implements AuthenticationPasswordPolicyHandlingStrategy<AuthnResponse, PasswordPolicyContext> {

    @Override
    public @Nullable List<MessageDescriptor> handle(@Nullable final AuthnResponse response,
                                                    @Nullable final PasswordPolicyContext configuration) throws Throwable {
        if (configuration == null) {
            LOGGER.debug("No password policy configuration is defined");
            return new ArrayList<>();
        }
        val accountStateHandler = configuration.getAccountStateHandler();
        if (accountStateHandler == null) {
            LOGGER.debug("No password policy account state handler is defined");
            return new ArrayList<>();
        }

        LOGGER.debug("Applying password policy [{}] to [{}]", response, accountStateHandler);
        return accountStateHandler.handle(response, configuration);
    }
}
