package org.apereo.cas.authentication.surrogate;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link JsonResourceSurrogateAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class JsonResourceSurrogateAuthenticationService extends SimpleSurrogateAuthenticationService implements DisposableBean {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();
    
    private final WatcherService watcherService;

    public JsonResourceSurrogateAuthenticationService(final File json, final ServicesManager servicesManager,
                                                      final CasConfigurationProperties casProperties,
                                                      final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer,
                                                      final ConfigurableApplicationContext applicationContext) throws Exception {
        super(readAccountsFromFile(json), servicesManager, casProperties, principalAccessStrategyEnforcer, applicationContext);
        this.watcherService = new FileWatcherService(json, this::loadServices);
        this.watcherService.start(getClass().getSimpleName());
    }

    public JsonResourceSurrogateAuthenticationService(final ServicesManager servicesManager,
                                                      final CasConfigurationProperties casProperties,
                                                      final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer,
                                                      final ConfigurableApplicationContext applicationContext) throws Exception {
        this(casProperties.getAuthn().getSurrogate().getJson().getLocation().getFile(), servicesManager, casProperties,
            principalAccessStrategyEnforcer, applicationContext);
    }

    private static Map readAccountsFromFile(final File json) throws Exception {
        return MAPPER.readValue(json, Map.class);
    }

    private void loadServices(final File file) {
        FunctionUtils.doAndHandle(_ -> {
            getEligibleAccounts().clear();
            getEligibleAccounts().putAll(readAccountsFromFile(file));
        });
    }

    @Override
    public void destroy() {
        if (watcherService != null) {
            watcherService.close();
        }
    }
}
