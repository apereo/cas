package org.apereo.cas.services.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link RegisteredServiceYamlSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class RegisteredServiceYamlSerializer extends RegisteredServiceJsonSerializer {
    private static final long serialVersionUID = -6026921045861422473L;

    @Override
    protected JsonFactory getJsonFactory() {
        return new YAMLFactory();
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
}
