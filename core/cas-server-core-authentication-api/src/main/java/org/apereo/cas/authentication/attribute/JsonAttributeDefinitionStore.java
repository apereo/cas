package org.apereo.cas.authentication.attribute;

import module java.base;
import org.apereo.cas.util.ResourceUtils;
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

    private @Nullable FileWatcherService storeWatcherService;
    private @Nullable Resource resource;

    public JsonAttributeDefinitionStore(final Resource resource) throws Exception {
        if (ResourceUtils.doesResourceExist(resource)) {
            this.resource = resource;
            importStore(resource);
            watchStore();
        }
    }

    public JsonAttributeDefinitionStore(final AttributeDefinition... definitions) {
        super(definitions);
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
     * @throws Exception the exception
     */
    public void watchStore() throws Exception {
        if (resource != null && ResourceUtils.isFile(resource)) {
            this.storeWatcherService = new FileWatcherService(resource.getFile(),
                file -> importStore(new FileSystemResource(file)));
            this.storeWatcherService.start(getClass().getSimpleName());
        }
    }
}
