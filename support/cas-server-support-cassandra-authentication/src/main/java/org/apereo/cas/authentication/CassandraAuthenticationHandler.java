package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.cassandra.authentication.CassandraAuthenticationProperties;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import java.util.ArrayList;

/**
 * This is {@link CassandraAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class CassandraAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private final CassandraAuthenticationProperties cassandraAuthenticationProperties;
    private final CassandraRepository cassandraRepository;

    public CassandraAuthenticationHandler(final String name,
                                          final PrincipalFactory principalFactory, final Integer order,
                                          final CassandraAuthenticationProperties cassandraAuthenticationProperties,
                                          final CassandraRepository cassandraRepository) {
        super(name, principalFactory, order);
        this.cassandraAuthenticationProperties = cassandraAuthenticationProperties;
        this.cassandraRepository = cassandraRepository;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword) throws Throwable {
        val username = credential.getUsername();
        val attributes = this.cassandraRepository.getUser(username);

        if (attributes == null || attributes.isEmpty()
            || !attributes.containsKey(cassandraAuthenticationProperties.getUsernameAttribute())
            || !attributes.containsKey(cassandraAuthenticationProperties.getPasswordAttribute())) {
            LOGGER.warn("Unable to find account [{}]: The account does not exist or it's missing username/password attributes", username);
            throw new AccountNotFoundException();
        }

        LOGGER.debug("Located account attributes [{}] for [{}]", attributes.keySet(), username);
        val entryPassword = attributes.get(cassandraAuthenticationProperties.getPasswordAttribute()).getFirst().toString();

        if (!getPasswordEncoder().matches(originalPassword, entryPassword)) {
            LOGGER.warn("Account password on record for [{}] does not match the given password", username);
            throw new FailedLoginException();
        }
        val principal = this.principalFactory.createPrincipal(username, attributes);
        return createHandlerResult(credential, principal, new ArrayList<>());
    }
}
