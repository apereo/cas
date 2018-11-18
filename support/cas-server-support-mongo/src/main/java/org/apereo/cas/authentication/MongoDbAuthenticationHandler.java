package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.integration.pac4j.authentication.handler.support.UsernamePasswordWrapperAuthenticationHandler;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.mongo.profile.service.MongoProfileService;
import org.springframework.beans.factory.DisposableBean;

/**
 * An authentication handler to verify credentials against a MongoDb instance.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
public class MongoDbAuthenticationHandler extends UsernamePasswordWrapperAuthenticationHandler implements AutoCloseable, DisposableBean {
    public MongoDbAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory) {
        super(name, servicesManager, principalFactory, null);
    }

    @Override
    public void destroy() {
        close();
    }

    @Override
    public void close() {
        try {
            val service = MongoProfileService.class.cast(authenticator);
            service.getMongoClient().close();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
