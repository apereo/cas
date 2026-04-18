package org.apereo.cas.authentication.attribute;

import module java.base;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * This is {@link JsonAttributeDefinitionStore}.
 *
 * @author Misagh Moayyed
 * @author Travis Schmidt
 * @since 6.2.0
 */
@Slf4j
@SuppressWarnings("NullAway.Init")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class JsonAttributeDefinitionStore extends AbstractAttributeDefinitionStore {

    private FileWatcherService storeWatcherService;

    public JsonAttributeDefinitionStore(final Resource resource) throws Exception {
        if (ResourceUtils.doesResourceExist(resource)) {
            importStore(resource);
            watchStore(resource);
        }
    }

    public JsonAttributeDefinitionStore(final AttributeDefinition... definitions) {
        super(definitions);
    }

    @Override
    @CanIgnoreReturnValue
    public AttributeDefinitionStore store(final Resource resource) {
        return FunctionUtils.doUnchecked(() -> {
            val json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(getAttributeDefinitionsMap());
            LOGGER.trace("Storing attribute definitions as [{}] to [{}]", json, resource);
            try (val writer = Files.newBufferedWriter(resource.getFile().toPath(), StandardCharsets.UTF_8)) {
                writer.write(json);
                writer.flush();
            }
            return this;
        });
    }
    
    @Override
    public void close() {
        if (this.storeWatcherService != null) {
            this.storeWatcherService.close();
        }
    }
    
    
    /**
     * Watch store.
     *
     * @param resource the resource
     * @throws Exception the exception
     */
    public void watchStore(final Resource resource) throws Exception {
        if (ResourceUtils.isFile(resource)) {
            this.storeWatcherService = new FileWatcherService(resource.getFile(),
                file -> importStore(new FileSystemResource(file)));
            this.storeWatcherService.start(getClass().getSimpleName());
        }
    }
}
