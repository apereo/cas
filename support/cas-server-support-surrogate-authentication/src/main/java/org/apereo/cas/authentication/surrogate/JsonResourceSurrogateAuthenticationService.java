package org.apereo.cas.authentication.surrogate;

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
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    public JsonResourceSurrogateAuthenticationService(final File json) throws Exception {
        super(MAPPER.readValue(json, Map.class));
    }

    public JsonResourceSurrogateAuthenticationService(final Resource json) throws Exception {
        this(json.getFile());
    }
}
