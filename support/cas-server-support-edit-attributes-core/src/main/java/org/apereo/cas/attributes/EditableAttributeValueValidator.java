package org.apereo.cas.attributes;

import java.util.List;
import java.util.Map;

import org.apereo.cas.configuration.model.support.attributes.EditableAttributeProperties.EditableAttribute;

/**
 * Validates attribute values.
 * 
 * @author Marcus Watkins
 * @since 5.2
 *
 */
public interface EditableAttributeValueValidator {

    /**
     * Checks whether all attribute values are valid.
     * 
     * @param attributes Attribute settings
     * @param attributeValues Values to test
     * @return whether all attributes have valid values
     */
    boolean areAttributeValuesValid(List<EditableAttribute> attributes, Map<String, String> attributeValues);

}
