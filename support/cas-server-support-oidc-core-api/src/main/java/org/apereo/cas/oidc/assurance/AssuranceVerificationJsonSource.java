package org.apereo.cas.oidc.assurance;

import org.apereo.cas.oidc.assurance.entity.Verification;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link AssuranceVerificationJsonSource}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class AssuranceVerificationJsonSource implements AssuranceVerificationSource, DisposableBean, AutoCloseable {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final List<Verification> verifications = Collections.synchronizedList(new ArrayList<>());

    private FileWatcherService watcherService;

    public AssuranceVerificationJsonSource(final Resource resource) throws Exception {
        if (ResourceUtils.doesResourceExist(resource)) {
            loadFromInputStream(resource);
            if (ResourceUtils.isFile(resource)) {
                this.watcherService = new FileWatcherService(resource.getFile(), __ -> loadFromInputStream(resource));
                this.watcherService.start(getClass().getSimpleName());
            }
        }
    }

    @Override
    public List<Verification> load() {
        return List.copyOf(verifications);
    }

    @Override
    public Optional<Verification> findByTrustFramework(final String trustFramework) {
        return verifications.stream()
            .filter(verification -> StringUtils.equalsIgnoreCase(trustFramework, verification.getTrustFramework()))
            .findFirst();
    }

    @Override
    public void close() {
        if (this.watcherService != null) {
            this.watcherService.close();
        }
    }

    @Override
    public void destroy() {
        close();
    }

    private void loadFromInputStream(final Resource resource) {
        FunctionUtils.doAndHandle(__ -> {
            try (val is = resource.getInputStream()) {
                val json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                val results = MAPPER.readValue(JsonValue.readHjson(json).toString(), new TypeReference<List<Verification>>() {
                });
                verifications.clear();
                verifications.addAll(results);
            }
        });
    }
}
