package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.Map;

/**
 * This is {@link JsonResourceSurrogateAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class JsonResourceSurrogateAuthenticationService extends SimpleSurrogateAuthenticationService {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    public JsonResourceSurrogateAuthenticationService(final File json, final ServicesManager servicesManager) throws Exception {
        super(MAPPER.readValue(json, Map.class), servicesManager);
    }

    public JsonResourceSurrogateAuthenticationService(final Resource json, final ServicesManager servicesManager) throws Exception {
        this(json.getFile(), servicesManager);
    }
}
