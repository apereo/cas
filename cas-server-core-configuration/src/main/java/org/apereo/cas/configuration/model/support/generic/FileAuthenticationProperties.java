package org.apereo.cas.configuration.model.support.generic;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * This is {@link FileAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "file.authn", ignoreUnknownFields = false)
public class FileAuthenticationProperties {
    
    private Resource filename;
    private String separator = "::";

    public Resource getFilename() {
        return filename;
    }

    public void setFilename(final Resource filename) {
        this.filename = filename;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(final String separator) {
        this.separator = separator;
    }
}
