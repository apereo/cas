package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Optional;

/**
 * Generates a unique consistent Id based on the principal.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface PersistentIdGenerator extends Serializable {

    /**
     * Generates a PersistentId based on some algorithm plus the principal.
     *
     * @param principal the principal to generate the id for.
     * @param service   the service for which the id may be generated.
     * @return the generated persistent id.
     */
    String generate(String principal, String service);

    /**
     * Generates a PersistentId based on some algorithm plus the principal.
     *
     * @param principal the principal to generate the id for.
     * @param service   the service for which the id may be generated.
     * @return the generated persistent id.
     */
    default String generate(Principal principal, Service service) {
        return generate(principal, Optional.ofNullable(service).map(Principal::getId).orElse(null));
    }

    /**
     * Generates a PersistentId based on some algorithm plus the principal.
     *
     * @param principal the principal to generate the id for.
     * @param service   the service for which the id may be generated.
     * @return the generated persistent id.
     */
    String generate(Principal principal, String service);

    /**
     * Generates a PersistentId based on some algorithm plus the principal.
     *
     * @param principal the principal to generate the id for.
     * @param service   the service for which the id may be generated.
     * @return the generated persistent id.
     */
    default String generate(final String principal, final Service service) {
        return generate(principal, service.getId());
    }

    /**
     * Generate string.
     *
     * @param principal the principal
     * @return the string
     */
    default String generate(final Principal principal) {
        return generate(principal, StringUtils.EMPTY);
    }
}
