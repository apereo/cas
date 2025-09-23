package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import java.util.List;

/**
 * This is {@link AggregatingPersonAttributeDao}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@FunctionalInterface
public interface AggregatingPersonAttributeDao {
    /**
     * Get person attribute daos list.
     *
     * @return the list
     */
    List<PersonAttributeDao> getPersonAttributeDaos();
}
