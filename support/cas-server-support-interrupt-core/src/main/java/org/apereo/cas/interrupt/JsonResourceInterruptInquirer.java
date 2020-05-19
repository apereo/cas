package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.io.FileWatcherService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hjson.JsonValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;
import org.springframework.webflow.execution.RequestContext;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link JsonResourceInterruptInquirer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class JsonResourceInterruptInquirer extends BaseInterruptInquirer implements DisposableBean {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private FileWatcherService keystorePatchWatcherService;

    private final Resource resource;
    private final Map<String, InterruptResponse> interrupts = new ConcurrentHashMap<>();

    public JsonResourceInterruptInquirer(final Resource resource) {
        this.resource = resource;
        try {
            if (ResourceUtils.isFile(this.resource)) {
                val resourceFile = this.resource.getFile();
                keystorePatchWatcherService = new FileWatcherService(resourceFile, file -> readResourceForInterrupts());
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
    }

    @Override
    public InterruptResponse inquireInternal(final Authentication authentication,
                                             final RegisteredService registeredService,
                                             final Service service,
                                             final Credential credential,
                                             final RequestContext requestContext) {
        readResourceForInterrupts();
        val user = authentication.getPrincipal().getId();
        if (interrupts.containsKey(user)) {
            return interrupts.get(user);
        }
        return InterruptResponse.none();
    }

    @SneakyThrows
    private void readResourceForInterrupts() {
        this.interrupts.clear();
        if (ResourceUtils.doesResourceExist(resource)) {
            try (val reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                final TypeReference<Map<String, InterruptResponse>> personList = new TypeReference<>() {
                };
                val data = (Map) MAPPER.readValue(JsonValue.readHjson(reader).toString(), personList);
                this.interrupts.putAll(data);
            }
        }
    }

    @Override
    public void destroy() {
        if (this.keystorePatchWatcherService != null) {
            this.keystorePatchWatcherService.close();
        }
    }
}
