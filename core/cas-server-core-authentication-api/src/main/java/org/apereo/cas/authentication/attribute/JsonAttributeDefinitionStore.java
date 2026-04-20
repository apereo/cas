package org.apereo.cas.authentication.attribute;

import module java.base;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.io.FileWatcherService;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
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

    private static final AttributeDefinition[] EMPTY_DEFINITIONS_ARRAY = new AttributeDefinition[0];

    private @Nullable FileWatcherService storeWatcherService;
    private @Nullable Resource resource;

    public JsonAttributeDefinitionStore(final Resource resource) throws Exception {
        importDefinitionsFromResource(resource);
    }

    public JsonAttributeDefinitionStore(final AttributeDefinition... definitions) {
        super(definitions);
    }

    public JsonAttributeDefinitionStore(final Resource resource, final List<AttributeDefinition> definitions) {
        this(definitions.toArray(EMPTY_DEFINITIONS_ARRAY));
        importDefinitionsFromResource(resource);
    }

    @Override
    public void close() {
        if (storeWatcherService != null) {
            storeWatcherService.close();
        }
    }

    /**
     * Watch store.
     */
    private void watchStore() {
        FunctionUtils.doUnchecked(u -> {
            if (resource != null && ResourceUtils.isFile(resource)) {
                storeWatcherService = new FileWatcherService(resource.getFile(),
                    file -> importStore(new FileSystemResource(file)));
                storeWatcherService.start(getClass().getSimpleName());
            }
        });
    }

    @Override
    public void save() {
        export(resource);
    }

    private void importDefinitionsFromResource(final Resource resource) {
        this.resource = resource;
        importStore(resource);
        watchStore();
    }
}
