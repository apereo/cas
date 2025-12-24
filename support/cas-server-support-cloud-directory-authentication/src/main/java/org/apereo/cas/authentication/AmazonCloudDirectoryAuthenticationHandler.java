package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.clouddirectory.AmazonCloudDirectoryRepository;
import org.apereo.cas.configuration.model.support.clouddirectory.AmazonCloudDirectoryProperties;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link AmazonCloudDirectoryAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class AmazonCloudDirectoryAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private final AmazonCloudDirectoryRepository repository;
    private final AmazonCloudDirectoryProperties cloudDirectoryProperties;

    public AmazonCloudDirectoryAuthenticationHandler(final String name,

                                                     final PrincipalFactory principalFactory,
                                                     final AmazonCloudDirectoryRepository repository,
                                                     final AmazonCloudDirectoryProperties cloudDirectoryProperties) {
        super(name, principalFactory, cloudDirectoryProperties.getOrder());
        this.repository = repository;
        this.cloudDirectoryProperties = cloudDirectoryProperties;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        @Nullable final String originalPassword) throws Throwable {

        val username = credential.getUsername();

        val attributes = repository.getUser(username);

        if (attributes == null || attributes.isEmpty()
            || !attributes.containsKey(cloudDirectoryProperties.getUsernameAttributeName())
            || !attributes.containsKey(cloudDirectoryProperties.getPasswordAttributeName())) {
            LOGGER.warn("Unable to find account [{}]: The account does not exist or it's missing username/password attributes", username);
            throw new AccountNotFoundException();
        }

        LOGGER.debug("Located account attributes [{}] for [{}]", attributes.keySet(), username);

        val userPassword = attributes.get(cloudDirectoryProperties.getPasswordAttributeName()).getFirst().toString();
        if (!matches(Objects.requireNonNull(originalPassword), userPassword)) {
            LOGGER.warn("Account password on record for [{}] does not match the given/encoded password", username);
            throw new FailedLoginException();
        }
        val principal = this.principalFactory.createPrincipal(username, attributes);
        return createHandlerResult(credential, principal, new ArrayList<>());
    }
}
