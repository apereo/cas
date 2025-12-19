package org.apereo.cas.authentication.attribute;

import module java.base;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link AttributeDefinition}.
 *
 * @author Misagh Moayyed
 * @author Travis Schmidt
 * @since 6.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface AttributeDefinition extends Serializable, Comparable<AttributeDefinition> {

    /**
     * Attribute key (original name) that should be used to register
     * this definition into the attribute store.
     *
     * @return the key
     */
    String getKey();

    /**
     * Gets name that may be the same as the key, used for rendering
     * the attribute in responses that have a name concept.
     *
     * @return the name
     */
    String getName();

    /**
     * Indicate if the attribute value should
     * be scoped based on the scope defined in the CAS configuration.
     *
     * @return true/false
     */
    boolean isScoped();

    /**
     * Indicate if the attribute value should
     * be encrypted using defined public keys for the service.
     *
     * @return true/false
     */
    boolean isEncrypted();

    /**
     * Indicate if the attribute value should be rendered a single element
     * if the container that carries the attribute values has a size of 1.
     * @return true/false
     */
    boolean isSingleValue();

    /**
     * Gets underlying source attribute that should drive
     * the value of the attribute definition.
     *
     * @return the attribute
     */
    String getAttribute();

    /**
     * Template used in {@link java.text.MessageFormat} that will insert attribute value into the template.
     *
     * @return the template
     */
    String getPatternFormat();

    /**
     * Groovy script definition (embedded or external) that should be invoked to determine
     * the attribute value for this definition.
     *
     * @return the script
     */
    String getScript();

    /**
     * When constructing the final attribute value(s),
     * indicate how each value should be canonicalized.
     * Accepted values are:
     * <ul>
     * <li>{@code UPPER}: Transform the value into uppercase characters.</li>
     * <li>{@code LOWER}: Transform the value into lowercase characters.</li>
     * <li>{@code NONE}: Do nothing.</li>
     * </ul>
     *
     * @return the canonicalization mode
     */
    String getCanonicalizationMode();

    /**
     * A map of regular expression patterns to values.
     * If an attribute definition is to build its values off of an existing attribute,
     * each value is examined against patterns defined here. For each match, the linked entry
     * is used to determine the attribute definition value, either statically or dynamically
     * which is typically an inlined Groovy script.
     *
     * @return patterned values map
     */
    Map<String, String> getPatterns();


    /**
     * Flatten the final values produced for this definition
     * into a single value, and separate the results by the assigned delimiter.
     *
     * @return the flattened
     */
    String getFlattened();

    /**
     * Resolve attribute values as list.
     *
     * @param context the context
     * @return the list
     * @throws Throwable the throwable
     */
    List<Object> resolveAttributeValues(AttributeDefinitionResolutionContext context) throws Throwable;

    /**
     * To attribute value.
     *
     * @param givenValues the given values
     * @return the object
     */
    @JsonIgnore
    default Object toAttributeValue(final Object givenValues) {
        return isSingleValue() && givenValues instanceof final Collection values && values.size() == 1
            ? values.iterator().next()
            : givenValues;
    }
}
