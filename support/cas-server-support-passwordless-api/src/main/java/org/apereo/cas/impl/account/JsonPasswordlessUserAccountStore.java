package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountCustomizer;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.io.WatcherService;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.lambda.Unchecked;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

/**
 * This is {@link GroovyPasswordlessUserAccountStore}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class JsonPasswordlessUserAccountStore extends SimplePasswordlessUserAccountStore {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).singleValueAsArray(true).build().toObjectMapper();

    private WatcherService watcherService;

    private final Resource resource;

    public JsonPasswordlessUserAccountStore(final Resource resource,
                                            final ConfigurableApplicationContext applicationContext,
                                            final List<PasswordlessUserAccountCustomizer> customizerList) {
        super(readFromResource(resource), applicationContext, customizerList);
        this.resource = resource;
        Unchecked.consumer(res -> {
            if (ResourceUtils.isFile(resource)) {
                watcherService = new FileWatcherService(resource.getFile(), file -> reload());
                watcherService.start(getClass().getSimpleName());
            }
        }).accept(resource);
    }

    @Override
    public void reload() {
        accounts.clear();
        accounts.putAll(readFromResource(this.resource));
    }

    private static Map<String, PasswordlessUserAccount> readFromResource(final Resource resource) {
        return FunctionUtils.doUnchecked(() -> {
            try (var in = resource.getInputStream()) {
                return MAPPER.readValue(in, new TypeReference<>() {
                });
            }
        });
    }
}
