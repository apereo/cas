package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.mongo.MongoDbAuthenticationProperties;
import org.apereo.cas.util.CollectionUtils;

import com.mongodb.client.model.Filters;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.mongodb.core.MongoOperations;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An authentication handler to verify credentials against a MongoDb instance.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
public class MongoDbAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private final MongoOperations mongoTemplate;

    private final MongoDbAuthenticationProperties properties;

    public MongoDbAuthenticationHandler(final String name,
                                        final PrincipalFactory principalFactory,
                                        final MongoDbAuthenticationProperties properties,
                                        final MongoOperations mongoTemplate) {
        super(name, principalFactory, properties.getOrder());
        this.mongoTemplate = mongoTemplate;
        this.properties = properties;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential transformedCredential,
                                                                                        final String originalPassword) throws Throwable {
        val collection = mongoTemplate.getCollection(properties.getCollection());
        try (val it = collection.find(Filters.eq(properties.getUsernameAttribute(), transformedCredential.getUsername())).iterator()) {
            if (it.hasNext()) {
                val result = it.next();
                if (!result.containsKey(properties.getPasswordAttribute())) {
                    throw new FailedLoginException("No password attribute found for " + transformedCredential.getId());
                }

                val entryPassword = result.get(properties.getPasswordAttribute());
                if (!getPasswordEncoder().matches(originalPassword, entryPassword.toString())) {
                    LOGGER.warn("Account password on record for [{}] does not match the given/encoded password", transformedCredential.getId());
                    throw new FailedLoginException();
                }
                val attributes = result
                    .entrySet()
                    .stream()
                    .filter(entry -> !entry.getKey().equals(properties.getPasswordAttribute()) && !entry.getKey().equals(properties.getUsernameAttribute()))
                    .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> CollectionUtils.toCollection(entry.getValue(), ArrayList.class), (__, b) -> b, () -> new HashMap<String, List<Object>>()));
                val principal = this.principalFactory.createPrincipal(transformedCredential.getId(), attributes);
                return createHandlerResult(transformedCredential, principal, new ArrayList<>());
            }
            throw new AccountNotFoundException("Unable to locate user account");
        }
    }
}
