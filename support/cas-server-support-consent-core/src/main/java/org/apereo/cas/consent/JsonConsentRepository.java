package org.apereo.cas.consent;

import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.val;
import org.hjson.JsonValue;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.fi.util.function.CheckedConsumer;
import org.jooq.lambda.fi.util.function.CheckedSupplier;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link JsonConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
public class JsonConsentRepository extends BaseConsentRepository implements DisposableBean {
    @Serial
    private static final long serialVersionUID = -402728417464783825L;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final Resource jsonResource;

    private WatcherService watcherService;

    public JsonConsentRepository(final Resource resource) throws Exception {
        this.jsonResource = resource;
        setConsentDecisions(readDecisionsFromJsonResource());
        if (ResourceUtils.isFile(this.jsonResource)) {
            this.watcherService = new FileWatcherService(resource.getFile(),
                __ -> setConsentDecisions(readDecisionsFromJsonResource()));
            this.watcherService.start(getClass().getSimpleName());
        }
    }

    @Override
    public void destroy() {
        FunctionUtils.doIfNotNull(watcherService, WatcherService::close);
    }

    @Override
    public ConsentDecision storeConsentDecision(final ConsentDecision decision) throws Throwable {
        val result = super.storeConsentDecision(decision);
        writeAccountToJsonResource();
        return result;
    }

    @Override
    public boolean deleteConsentDecision(final long decisionId, final String principal) throws Throwable {
        val result = super.deleteConsentDecision(decisionId, principal);
        writeAccountToJsonResource();
        return result;
    }

    @Override
    public boolean deleteConsentDecisions(final String principal) throws Throwable {
        val result = super.deleteConsentDecisions(principal);
        writeAccountToJsonResource();
        return result;
    }

    @Override
    public void deleteAll() throws Throwable {
        super.deleteAll();
        writeAccountToJsonResource();
    }

    private Set<ConsentDecision> readDecisionsFromJsonResource() {
        return FunctionUtils.doAndHandle((CheckedSupplier<Set<ConsentDecision>>) () -> {
            if (ResourceUtils.doesResourceExist(jsonResource)) {
                try (val reader = new InputStreamReader(jsonResource.getInputStream(), StandardCharsets.UTF_8)) {
                    val personList = new TypeReference<Set<ConsentDecision>>() {
                    };
                    return MAPPER.readValue(JsonValue.readHjson(reader).toString(), personList);
                }
            }
            return new LinkedHashSet<>();
        }, throwable -> new LinkedHashSet<>()).get();
    }

    private void writeAccountToJsonResource() {
        Unchecked.consumer((CheckedConsumer<Resource>) resource -> {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(resource.getFile(), getConsentDecisions());
            readDecisionsFromJsonResource();
        }).accept(this.jsonResource);
    }
}
