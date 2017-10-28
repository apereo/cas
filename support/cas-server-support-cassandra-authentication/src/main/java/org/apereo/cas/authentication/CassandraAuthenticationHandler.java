package org.apereo.cas.authentication;

import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.cassandra.authentication.CassandraAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Map;

/**
 * This is {@link CassandraAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CassandraAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraAuthenticationHandler.class);
    private final CassandraAuthenticationProperties cassandraAuthenticationProperties;
    private final CassandraRepository cassandraRepository;

    public CassandraAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                          final PrincipalFactory principalFactory, final Integer order,
                                          final CassandraAuthenticationProperties cassandraAuthenticationProperties,
                                          final CassandraRepository cassandraRepository) {
        super(name, servicesManager, principalFactory, order);
        this.cassandraAuthenticationProperties = cassandraAuthenticationProperties;
        this.cassandraRepository = cassandraRepository;
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                 final String originalPassword) throws GeneralSecurityException {
        final String username = credential.getUsername();
        final String password = credential.getPassword();

        final Map<String, Object> attributes = this.cassandraRepository.getUser(username);

        if (attributes == null || attributes.isEmpty()
                || !attributes.containsKey(cassandraAuthenticationProperties.getUsernameAttribute())
                || !attributes.containsKey(cassandraAuthenticationProperties.getPasswordAttribute())) {
            LOGGER.warn("Unable to find account [{}]: The account does not exist or it's missing username/password attributes", username);
            throw new AccountNotFoundException();
        }

        LOGGER.debug("Located account attributes [{}] for [{}]", attributes.keySet(), username);
        final String userPassword = attributes.get(cassandraAuthenticationProperties.getPasswordAttribute()).toString();
        if (!password.equals(userPassword)) {
            LOGGER.warn("Account password on record for [{}] does not match the given password", username);
            throw new FailedLoginException();
        }
        return createHandlerResult(credential,
                this.principalFactory.createPrincipal(username, attributes), new ArrayList<>());
    }
}
