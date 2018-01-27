package org.apereo.cas.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.integration.pac4j.authentication.handler.support.UsernamePasswordWrapperAuthenticationHandler;
import org.apereo.cas.services.ServicesManager;
import org.pac4j.mongo.profile.service.MongoProfileService;

import javax.annotation.PreDestroy;
import java.io.Closeable;

/**
 * An authentication handler to verify credentials against a MongoDb instance.
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
public class MongoDbAuthenticationHandler extends UsernamePasswordWrapperAuthenticationHandler implements Closeable {
    public MongoDbAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory) {
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
