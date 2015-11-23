package org.jasig.cas.authentication.principal;

import java.io.Serializable;

/**
 * Generates a unique consistent Id based on the principal.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public interface PersistentIdGenerator extends Serializable {

    /**
     * Generates a PersistentId based on some algorithm plus the principal.
     *
     * @param principal the principal to generate the id for.
     * @param service the service for which the id may be generated.
     * @return the generated persistent id.
     */
    String generate(Principal principal, Service service);
}
