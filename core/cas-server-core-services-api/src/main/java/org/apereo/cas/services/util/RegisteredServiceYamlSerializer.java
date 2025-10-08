package org.apereo.cas.services.util;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import tools.jackson.core.TokenStreamFactory;
import tools.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * This is {@link RegisteredServiceYamlSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class RegisteredServiceYamlSerializer extends RegisteredServiceJsonSerializer {
    @Serial
    private static final long serialVersionUID = -6026921045861422473L;

    public RegisteredServiceYamlSerializer(final ConfigurableApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public boolean supports(final File file) {
        try {
            val contents = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name()).trim();
            return supports(contents);
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean supports(final String content) {
        return content.startsWith("--- !<");
    }

    @Override
    public List<MediaType> getContentTypes() {
        return List.of(MediaType.valueOf("application/yaml"), MediaType.valueOf("application/yml"));
    }

    @Override
    protected TokenStreamFactory getJsonFactory() {
        return new YAMLFactory();
    }
}
