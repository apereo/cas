package org.apereo.cas.authentication.support;

import org.apereo.cas.authentication.MessageDescriptor;
import org.ldaptive.auth.AuthenticationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DefaultLdapPasswordPolicyHandlingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultLdapPasswordPolicyHandlingStrategy implements LdapPasswordPolicyHandlingStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLdapPasswordPolicyHandlingStrategy.class);

    @Override
    public List<MessageDescriptor> handle(final AuthenticationResponse response,
                                          final LdapPasswordPolicyConfiguration configuration) throws LoginException {
        if (configuration == null) {
            LOGGER.debug("No ldap password policy configuration is defined");
            return new ArrayList<>(0);
        }
        final AccountStateHandler accountStateHandler = configuration.getAccountStateHandler();
        LOGGER.debug("Applying password policy [{}] to [{}]", response, accountStateHandler);
        return accountStateHandler.handle(response, configuration);
    }
}
