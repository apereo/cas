package org.apereo.cas.authentication.principal.attribute;
import module java.base;

/**
 * This is {@link PersonAttributeDaoFilter}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@FunctionalInterface
public interface PersonAttributeDaoFilter {
    /**
     * Choose person attribute dao.
     *
     * @param personAttributeDao the person attribute dao
     * @return true/false
     */
    boolean choosePersonAttributeDao(PersonAttributeDao personAttributeDao);

    /**
     * Always choose person attribute dao filter.
     *
     * @return the person attribute dao filter
     */
    static PersonAttributeDaoFilter alwaysChoose() {
        return personAttributeDao -> true;
    }
}
