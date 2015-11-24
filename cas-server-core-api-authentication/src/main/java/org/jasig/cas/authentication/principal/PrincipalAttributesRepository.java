package org.jasig.cas.authentication.principal;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * Defines operations required for retrieving principal attributes.
 * Acts as a proxy between the external attribute source and CAS,
 * executing such as additional processing or caching on the set
 * of retrieved attributes. Implementations may simply decide to
 * do nothing on the set of attributes that the principal carries
 * or they may attempt to refresh them from the source, etc.
 * @author Misagh Moayyed
 * @see org.jasig.cas.authentication.principal.PrincipalFactory
 * @since 4.1
 */
public interface PrincipalAttributesRepository extends Serializable {

    /**
     * Gets attributes for the given principal id.
     *
     * @param p the principal whose attributes need to be retrieved.
     * @return the attributes
     */
    Map<String, Object> getAttributes(@NotNull Principal p);
}
