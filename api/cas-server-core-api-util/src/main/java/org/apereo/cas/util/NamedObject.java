package org.apereo.cas.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link NamedObject}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public interface NamedObject {
    /**
     * Gets name.
     *
     * @return the name
     */
    @JsonIgnore
    default String getName() {
        return StringUtils.defaultIfBlank(getClass().getSimpleName(), "Default");
    }
}
