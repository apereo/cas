package org.apereo.cas.authentication.principal.merger;

import module java.base;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;

/**
 * Interface for merging attributes from sibling attribute repositories.
 *
 * @author andrew.petro@yale.edu
 * @since 7.1.0
 */
public interface AttributeMerger extends Serializable {
    /**
     * Merge the results of a Set of {@link PersonAttributes} and a compiled results map.
     *
     * @param toModify The compiled results map, this will be modified based on the values in toConsider.
     * @param toConsider The query results map, this will not be modified.
     * @return Merged set of {@link PersonAttributes}
     */
    Set<PersonAttributes> mergeResults(Set<PersonAttributes> toModify, Set<PersonAttributes> toConsider);

    /**
     * Modify the "toModify" argument in consideration of the "toConsider" argument. Return the resulting Set which may
     * or may not be the same reference as the "toModify" argument.
     * <p>
     * The modification performed is implementation-specific -- implementations of this interface exist to perform some
     * particular transformation on the toModify argument given the toConsider argument.
     *
     * @param toModify Modify this set
     * @param toConsider In consideration of this set
     * @return The modified set
     * @throws IllegalArgumentException if either toModify or toConsider is null
     */
    Set<String> mergePossibleUserAttributeNames(Set<String> toModify, Set<String> toConsider);

    /**
     * Modify the "toModify" argument in consideration of the "toConsider" argument. Return the resulting Set which may
     * or may not be the same reference as the "toModify" argument.
     * <p>
     * The modification performed is implementation-specific -- implementations of this interface exist to perform some
     * particular transformation on the toModify argument given the toConsider argument.
     *
     * @param toModify Modify this set
     * @param toConsider In consideration of this set
     * @return The modified set
     * @throws IllegalArgumentException if either toModify or toConsider is null
     */
    Set<String> mergeAvailableQueryAttributes(Set<String> toModify, Set<String> toConsider);

    /**
     * Modify the "toModify" argument in consideration of the "toConsider" 
     * argument.  Return the resulting Map, which may or may not be the same
     * reference as the "toModify" argument.
     * The modification performed is implementation-specific -- implementations
     * of this interface exist to perform some particular transformation on
     * the toModify argument given the toConsider argument.
     *
     * @param toModify - modify this map
     * @param toConsider - in consideration of this map
     * @return the modified Map
     * @throws IllegalArgumentException if either toModify or toConsider is null
     */
    Map<String, List<Object>> mergeAttributes(Map<String, List<Object>> toModify, Map<String, List<Object>> toConsider);
}
