package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.clouddirectory.CloudDirectoryRepository;
import org.apereo.cas.configuration.model.support.clouddirectory.CloudDirectoryProperties;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

/**
 * This is {@link CloudDirectoryAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class CloudDirectoryAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private final CloudDirectoryRepository repository;
    private final CloudDirectoryProperties cloudDirectoryProperties;

    public CloudDirectoryAuthenticationHandler(final String name,
                                               final ServicesManager servicesManager,
                                               final PrincipalFactory principalFactory,
                                               final CloudDirectoryRepository repository,
                                               final CloudDirectoryProperties cloudDirectoryProperties) {
        super(name, servicesManager, principalFactory, cloudDirectoryProperties.getOrder());
        this.repository = repository;
        this.cloudDirectoryProperties = cloudDirectoryProperties;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword) throws GeneralSecurityException {

        val username = credential.getUsername();

        val attributes = repository.getUser(username);

        if (attributes == null || attributes.isEmpty()
            || !attributes.containsKey(cloudDirectoryProperties.getUsernameAttributeName())
            || !attributes.containsKey(cloudDirectoryProperties.getPasswordAttributeName())) {
            LOGGER.warn("Unable to find account [{}]: The account does not exist or it's missing username/password attributes", username);
            throw new AccountNotFoundException();
        }

        LOGGER.debug("Located account attributes [{}] for [{}]", attributes.keySet(), username);

        val userPassword = attributes.get(cloudDirectoryProperties.getPasswordAttributeName()).get(0).toString();
        if (!matches(originalPassword, userPassword)) {
            LOGGER.warn("Account password on record for [{}] does not match the given/encoded password", username);
            throw new FailedLoginException();
        }
        val principal = this.principalFactory.createPrincipal(username, attributes);
        return createHandlerResult(credential, principal, new ArrayList<>(0));
    }
}
