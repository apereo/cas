package org.apereo.cas.interrupt;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.hjson.JsonValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;
import org.springframework.webflow.execution.RequestContext;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link JsonResourceInterruptInquirer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class JsonResourceInterruptInquirer extends BaseInterruptInquirer implements DisposableBean {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final Resource resource;

    private final AtomicReference<Map<String, InterruptResponse>> interrupts = new AtomicReference<>(Map.of());

    private FileWatcherService keystorePatchWatcherService;

    public JsonResourceInterruptInquirer(final Resource resource) {
        this.resource = resource;
        readResourceForInterrupts();
        FunctionUtils.doUnchecked(_ -> {
            if (ResourceUtils.isFile(this.resource)) {
                keystorePatchWatcherService = new FileWatcherService(resource.getFile(),
                    _ -> readResourceForInterrupts(), _ -> readResourceForInterrupts(), _ -> {
                    });
                keystorePatchWatcherService.start(getClass().getSimpleName());
            }
        });
    }

    @Override
    public InterruptResponse inquireInternal(final Authentication authentication,
                                             final RegisteredService registeredService,
                                             final Service service,
                                             final Credential credential,
                                             final RequestContext requestContext) {
        val user = authentication.getPrincipal().getId();
        LOGGER.info("Locating interrupt for user [{}]", user);
        return Objects.requireNonNullElseGet(interrupts.get().get(user), InterruptResponse::none);
    }

    @Override
    public void destroy() {
        IOUtils.closeQuietly(this.keystorePatchWatcherService);
    }

    private void readResourceForInterrupts() {
        if (!ResourceUtils.doesResourceExist(resource)) {
            LOGGER.warn("Interrupt resource [{}] cannot be found; keeping the current interrupt definitions", resource);
            return;
        }
        try (val reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            val data = MAPPER.readValue(JsonValue.readHjson(reader).toString(), new TypeReference<Map<String, InterruptResponse>>() {
            });
            interrupts.set(Map.copyOf(data));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, "Unable to read interrupt resource [" + resource + "]; keeping the current interrupt definitions", e);
        }
    }
}
