package org.apereo.cas.interrupt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.ResourceUtils;
import org.hjson.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link JsonResourceInterruptInquirer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JsonResourceInterruptInquirer extends BaseInterruptInquirer {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonResourceInterruptInquirer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    
    private final Resource resource;
    private Map<String, InterruptResponse> interrupts;

    public JsonResourceInterruptInquirer(final Resource resource) {
        this.resource = resource;
    }

    @Override
    public InterruptResponse inquire(final Authentication authentication, final RegisteredService registeredService, final Service service) {
        final String user = authentication.getPrincipal().getId();
        readResourceForInterrupts();
        if (interrupts.containsKey(user)) {
            return interrupts.get(user);
        }
        return new InterruptResponse(false);
    }

    private void readResourceForInterrupts() {
        this.interrupts = new LinkedHashMap<>();
        if (ResourceUtils.doesResourceExist(resource)) {
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                final TypeReference<Map<String, InterruptResponse>> personList = new TypeReference<Map<String, InterruptResponse>>() {
                };
                this.interrupts = MAPPER.readValue(JsonValue.readHjson(reader).toString(), personList);
            } catch (final Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}
