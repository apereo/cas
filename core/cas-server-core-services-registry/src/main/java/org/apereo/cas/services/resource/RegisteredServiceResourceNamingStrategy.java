package org.apereo.cas.services.resource;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.RegexUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * This is {@link RegisteredServiceResourceNamingStrategy}.  Interface to provide naming strategy
 * to resource based services.
 *
 * @author Travis Schmidt
 * @since 5.3.0
 */
@FunctionalInterface
public interface RegisteredServiceResourceNamingStrategy {

    /**
     * Method will be called to provide a name for a resource to store a service.
     *
     * @param service   - The Service to be saved.
     * @param extension - The extension to be used.
     * @return - String representing a resource name.
     */
    String build(RegisteredService service, String extension);

    /**
     * Gets naming pattern.
     *
     * @param extensions the extensions
     * @return the naming pattern
     */
    default Pattern buildNamingPattern(final String... extensions) {
        var pattern = StringUtils.EMPTY;
        if (extensions.length > 0) {
            pattern = String.join("|", extensions);
        }
        if (extensions.length > 1) {
            pattern = '(' + pattern + ')';
        }
        return RegexUtils.createPattern("(\\w+)-(\\d+)\\.".concat(pattern));
    }
}
