package org.apereo.cas.authentication.attribute;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * This is {@link AttributeDefinition}.
 *
 * @author Misagh Moayyed
 * @author Travis Schmidt
 * @since 6.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonInclude(JsonInclude.Include.NON_NULL)
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
     * Gets name that may be the same as the key, used for rendering
     * the attribute in responses that have a friendly-name concept.
     *
     * @return the friendly name
     */
    String getFriendlyName();

    /**
     * Indicate if the attribute value should
     * be scoped based on the scope defined in the CAS configuration.
     *
     * @return the boolean
     */
    boolean isScoped();

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
}
