package org.apereo.cas.audit;

import module java.base;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apereo.inspektr.common.spi.PrincipalResolver;

/**
 * This is {@link AuditableEntity}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public interface AuditableEntity {

    /**
     * Gets principal.
     *
     * @return the principal
     */
    @JsonIgnore
    default String getAuditablePrincipal() {
        return PrincipalResolver.UNKNOWN_USER;
    }
}
