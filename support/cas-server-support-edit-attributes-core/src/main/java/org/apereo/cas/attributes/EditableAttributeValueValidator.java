package org.apereo.cas.attributes;

import java.util.List;
import java.util.Map;

import org.apereo.cas.configuration.model.support.attributes.EditableAttributeProperties.EditableAttribute;

public interface EditableAttributeValueValidator {

    /**
     * Checks whether all attribute values are valid.
     * 
     * @param attributes
     * @param attributeValues
     * @return
     */
    public boolean areAttributeValuesValid(List<EditableAttribute> attributes, Map<String, String> attributeValues);

}
