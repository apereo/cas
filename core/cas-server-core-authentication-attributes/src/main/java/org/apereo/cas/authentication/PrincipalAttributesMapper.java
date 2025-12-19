package org.apereo.cas.authentication;

import module java.base;

/**
 * This is {@link PrincipalAttributesMapper}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@FunctionalInterface
public interface PrincipalAttributesMapper {

    /**
     * Default principal attributes mapper.
     *
     * @return the principal attributes mapper
     */
    static PrincipalAttributesMapper defaultMapper() {
        return new DefaultPrincipalAttributesMapper();
    }

    /**
     * Map principal attributes.
     *
     * @param request the request
     * @return the map
     */
    Map<String, List<Object>> map(AttributeMappingRequest request);
}
