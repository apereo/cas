package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.Map;

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


    public JsonResourceSurrogateAuthenticationService(final File json, final ServicesManager servicesManager) throws Exception {
        super(readAccountsFromFile(json), servicesManager);
        this.watcherService = new FileWatcherService(json, this::loadServices);
        this.watcherService.start(getClass().getSimpleName());
    }

    public JsonResourceSurrogateAuthenticationService(final Resource json, final ServicesManager servicesManager) throws Exception {
        this(json.getFile(), servicesManager);
    }

    private static Map readAccountsFromFile(final File json) throws IOException {
        return MAPPER.readValue(json, Map.class);
    }

    private void loadServices(final File file) {
        FunctionUtils.doAndHandle(__ -> {
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
