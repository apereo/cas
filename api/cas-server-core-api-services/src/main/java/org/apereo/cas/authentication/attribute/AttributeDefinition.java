package org.apereo.cas.authentication.attribute;

import org.apereo.cas.services.RegisteredService;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.List;

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
     * Resolve attribute values as list.
     *
     * @param attributeValues   the attribute values
     * @param scope             the scope
     * @param registeredService the registered service
     * @return the list
     */
    List<Object> resolveAttributeValues(List<Object> attributeValues, String scope, RegisteredService registeredService);
}
