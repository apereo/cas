package org.apereo.cas.services.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link RegisteredServiceYamlSerializer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RegisteredServiceYamlSerializer extends DefaultRegisteredServiceJsonSerializer {
    private static final long serialVersionUID = -6026921045861422473L;

    @Override
    protected JsonFactory getJsonFactory() {
        return new YAMLFactory();
    }

    @Override
    public boolean supports(final File file) {
        try {
            final String contents = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name()).trim();
            return contents.startsWith("--- !<");
        } catch (final Exception e) {
            return false;
        }
    }
}
