package org.apereo.cas.adaptors.trusted.authentication.principal;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link RemoteRequestPrincipalAttributesExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface RemoteRequestPrincipalAttributesExtractor {

    /**
     * Gets attributes.
     *
     * @param request the request
     * @return the attributes
     */
    default Map<String, Object> getAttributes(final HttpServletRequest request) {
        return new HashMap<>(0);
    }
}
