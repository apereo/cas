package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.NamedObject;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.core.Ordered;

/**
 * This is {@link RegisteredServiceAttributeReleasePolicyPostProcessor}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceAttributeReleasePolicyPostProcessor extends Ordered, NamedObject {
    @Override
    default int getOrder() {
        return 0;
    }

    /**
     * Post process attributes that are about to be released.
     * {@code attributesToRelease} is a mutable map and can be modified in place.
     *
     * @param context             the context
     * @param attributesToRelease the attributes to release
     * @param availableAttributes the attributes to release
     */
    void process(RegisteredServiceAttributeReleasePolicyContext context,
                 Map<String, List<Object>> attributesToRelease,
                 Map<String, List<Object>> availableAttributes);
}
