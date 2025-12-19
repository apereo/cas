package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.services.replication.NoOpRegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategy;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.springframework.context.support.StaticApplicationContext;

/**
 * Test cases for {@link YamlServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("FileSystem")
class YamlServiceRegistryTests extends BaseResourceBasedServiceRegistryTests {

    @Override
    public ResourceBasedServiceRegistry getNewServiceRegistry() {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();
        newServiceRegistry = new YamlServiceRegistry(RESOURCE,
            WatcherService.noOp(),
            appCtx,
            new NoOpRegisteredServiceReplicationStrategy(),
            new DefaultRegisteredServiceResourceNamingStrategy(),
            new ArrayList<>());
        return newServiceRegistry;
    }

    @Override
    protected Stream<Class<? extends BaseWebBasedRegisteredService>> getRegisteredServiceTypes() {
        return Stream.of(
            CasRegisteredService.class,
            OAuthRegisteredService.class,
            SamlRegisteredService.class,
            OidcRegisteredService.class,
            WSFederationRegisteredService.class);
    }
}
