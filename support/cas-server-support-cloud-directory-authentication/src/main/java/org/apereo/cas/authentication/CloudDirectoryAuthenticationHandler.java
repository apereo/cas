package org.apereo.cas.authentication;

import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.clouddirectory.CloudDirectoryRepository;
import org.apereo.cas.configuration.model.support.clouddirectory.CloudDirectoryProperties;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Map;

/**
 * This is {@link CloudDirectoryAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CloudDirectoryAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudDirectoryAuthenticationHandler.class);

    private final CloudDirectoryRepository repository;
    private final CloudDirectoryProperties cloudDirectoryProperties;

    public CloudDirectoryAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                               final PrincipalFactory principalFactory,
                                               final CloudDirectoryRepository repository,
                                               final CloudDirectoryProperties cloudDirectoryProperties) {
        super(name, servicesManager, principalFactory, null);
        this.repository = repository;
        this.cloudDirectoryProperties = cloudDirectoryProperties;
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                 final String originalPassword) throws GeneralSecurityException {

        final String username = credential.getUsername();

        final Map<String, Object> attributes = repository.getUser(username);

        if (attributes == null || attributes.isEmpty()
                || !attributes.containsKey(cloudDirectoryProperties.getUsernameAttributeName())
                || !attributes.containsKey(cloudDirectoryProperties.getPasswordAttributeName())) {
            LOGGER.warn("Unable to find account [{}]: The account does not exist or it's missing username/password attributes", username);
            throw new AccountNotFoundException();
        }

        LOGGER.debug("Located account attributes [{}] for [{}]", attributes.keySet(), username);

        final String userPassword = attributes.get(cloudDirectoryProperties.getPasswordAttributeName()).toString();
        if (!matches(originalPassword, userPassword)) {
            LOGGER.warn("Account password on record for [{}] does not match the given/encoded password", username);
            throw new FailedLoginException();
        }
        return createHandlerResult(credential,
                this.principalFactory.createPrincipal(username, attributes), new ArrayList<>());
    }
}
