package org.apereo.cas.authentication.support;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.MessageDescriptor;
import org.ldaptive.auth.AuthenticationResponse;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DefaultLdapPasswordPolicyHandlingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class DefaultLdapPasswordPolicyHandlingStrategy implements LdapPasswordPolicyHandlingStrategy {


    @Override
    public List<MessageDescriptor> handle(final AuthenticationResponse response,
                                          final LdapPasswordPolicyConfiguration configuration) throws LoginException {
        if (configuration == null) {
            LOGGER.debug("No ldap password policy configuration is defined");
            return new ArrayList<>(0);
        }
        final LdapAccountStateHandler accountStateHandler = configuration.getAccountStateHandler();
        LOGGER.debug("Applying password policy [{}] to [{}]", response, accountStateHandler);
        return accountStateHandler.handle(response, configuration);
    }
}
