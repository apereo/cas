package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.mongo.MongoDbAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import com.mongodb.client.model.Filters;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An authentication handler to verify credentials against a MongoDb instance.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
public class MongoDbAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler implements AutoCloseable, DisposableBean {
    private final MongoTemplate mongoTemplate;

    private final MongoDbAuthenticationProperties properties;

    public MongoDbAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                        final PrincipalFactory principalFactory,
                                        final MongoDbAuthenticationProperties properties,
                                        final MongoTemplate mongoTemplate) {
        super(name, servicesManager, principalFactory, properties.getOrder());
        this.mongoTemplate = mongoTemplate;
        this.properties = properties;
    }

    @Override
    public void destroy() {
        close();
    }

    @Override
    public void close() {
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential transformedCredential,
                                                                                        final String originalPassword)
        throws GeneralSecurityException {

        val collection = mongoTemplate.getCollection(properties.getCollection());
        val it = collection.find(Filters.eq(properties.getUsernameAttribute(), transformedCredential.getUsername())).iterator();
        if (it.hasNext()) {
            val result = it.next();
            if (!result.containsKey(properties.getUsernameAttribute())) {
                throw new FailedLoginException("No user attribute found for " + transformedCredential.getId());
            }
            if (!result.containsKey(properties.getPasswordAttribute())) {
                throw new FailedLoginException("No password attribute found for " + transformedCredential.getId());
            }

            val entryPassword = result.get(properties.getPasswordAttribute());
            if (!getPasswordEncoder().matches(originalPassword, entryPassword.toString())) {
                LOGGER.warn("Account password on record for [{}] does not match the given/encoded password", transformedCredential.getId());
                throw new FailedLoginException();
            }
            val attributes = new HashMap<String, List<Object>>();
            result
                .entrySet()
                .stream()
                .filter(s ->
                    !s.getKey().equals(properties.getPasswordAttribute()) && !s.getKey().equals(properties.getUsernameAttribute()))
                .forEach(entry -> attributes.put(entry.getKey(),
                    CollectionUtils.toCollection(entry.getValue(), ArrayList.class)));
            val principal = this.principalFactory.createPrincipal(transformedCredential.getId(), attributes);
            return createHandlerResult(transformedCredential, principal, new ArrayList<>(0));
        }
        throw new FailedLoginException("Unable to locate user account");
    }
}
