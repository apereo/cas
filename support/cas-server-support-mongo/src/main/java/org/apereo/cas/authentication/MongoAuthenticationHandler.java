package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.integration.pac4j.authentication.handler.support.UsernamePasswordWrapperAuthenticationHandler;
import org.apereo.cas.services.ServicesManager;
import org.pac4j.mongo.profile.service.MongoProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.Closeable;

/**
 * An authentication handler to verify credentials against a MongoDb instance.
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class MongoAuthenticationHandler extends UsernamePasswordWrapperAuthenticationHandler implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoAuthenticationHandler.class);
    
    public MongoAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory) {
        super(name, servicesManager, principalFactory, null);
    }

    @PreDestroy
    @Override
    public void close() {
        try {
            final MongoProfileService service = MongoProfileService.class.cast(authenticator);
            service.getMongoClient().close();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
