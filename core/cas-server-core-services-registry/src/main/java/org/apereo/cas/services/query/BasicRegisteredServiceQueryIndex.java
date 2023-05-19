package org.apereo.cas.services.query;

import org.apereo.cas.services.RegisteredService;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.AttributeIndex;
import com.googlecode.cqengine.index.hash.HashIndex;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * This is {@link BasicRegisteredServiceQueryIndex}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Getter
@ToString
public class BasicRegisteredServiceQueryIndex implements RegisteredServiceQueryIndex<AttributeIndex<RegisteredService, Object>> {
    private final AttributeIndex index;

    /**
     * Hash index basic.
     *
     * @param attribute the attribute
     * @return the basic registered service query index
     */
    public static BasicRegisteredServiceQueryIndex hashIndex(final Attribute attribute) {
        return new BasicRegisteredServiceQueryIndex(HashIndex.onAttribute(attribute));
    }
}
