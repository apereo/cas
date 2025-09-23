package org.apereo.cas.persondir;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/**
 * A convenient wrapper around {@code ComplexStubPersonAttributeDao} that reads the configuration for its <i>backingMap</i>
 * property from an external JSON configuration resource.
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class JsonPersonAttributeDao extends MappablePersonAttributeDao implements Closeable, DisposableBean {

    private final Resource personAttributesConfigFile;

    @Setter
    private Closeable resourceWatcherService;

    private final ObjectMapper jacksonObjectMapper = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    /**
     * Init method un-marshals JSON representation of the person attributes.
     *
     * @throws IOException invalid config file URI
     */
    public void init() throws IOException {
        LOGGER.info("Un-marshaling person attributes from the config file [{}]", this.personAttributesConfigFile);
        try (val in = personAttributesConfigFile.getInputStream()) {
            val backingMap = jacksonObjectMapper.readValue(in, Map.class);
            LOGGER.debug("Person attributes have been successfully read into the map ");
            setBackingMap(backingMap);
        }
    }

    @Override
    public void close() throws IOException {
        if (this.resourceWatcherService != null) {
            resourceWatcherService.close();
        }
    }

    @Override
    public void destroy() throws Exception {
        close();
    }
}
