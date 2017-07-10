package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.core.Ordered;

import java.io.Serializable;
import java.util.Map;

/**
 * Defines the general contract of the attribute release policy for a registered service.
 * An instance of this attribute filter may determine how principal/global attributes are translated to a
 * map of attributes that may be released for a registered service.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public interface RegisteredServiceAttributeFilter extends Serializable, Ordered {
    /**
     * Filters the received principal attributes for the given registered service.
     *
     * @param givenAttributes the map for the original given attributes
     * @return a map that contains the filtered attributes.
     */
    Map<String, Object> filter(Map<String, Object> givenAttributes);

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
