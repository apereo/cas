package org.apereo.cas.authentication;

import java.util.List;
import java.util.Map;

/**
 * This is {@link PrincipalAttributesMapper}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@FunctionalInterface
public interface PrincipalAttributesMapper {

    Map<String, List<Object>> map(final AttributeMappingRequest request);

    static PrincipalAttributesMapper defaultMapper() {
        return new DefaultPrincipalAttributesMapper();
    }
}
