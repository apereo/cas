package org.apereo.cas.authentication.principal.attribute;

import module java.base;
import org.jspecify.annotations.Nullable;

/**
 * An immutable representation of a person with a uid (username) and attributes. A user's attributes can be of any type
 * and can be multi-valued.
 * <p>
 * {@link Principal#getName()} is used for the uid (username).
 * <p>
 * The equality and hashCode of an IPersonAttributes should ONLY include the name property and none of the attributes.
 *
 * @author Eric Dalquist
 * @since 7.1.0
 */
public interface PersonAttributes extends Principal, Serializable {
    /**
     * Gets attributes.
     *
     * @return The immutable Map of all attributes for the person.
     */
    Map<String, List<Object>> getAttributes();

    /**
     * The value for the attribute, null if no value exists or the first value is null, if there are multiple values
     * the first is returned.
     *
     * @param name The name of the attribute to get the value for
     * @return The first value for the attribute
     */
    @Nullable Object getAttributeValue(String name);

    /**
     * All values of the attribute, null if no values exist.
     *
     * @param name The name of the attribute to get the values for
     * @return All values for the attribute
     */
    @Nullable List<Object> getAttributeValues(String name);
}
