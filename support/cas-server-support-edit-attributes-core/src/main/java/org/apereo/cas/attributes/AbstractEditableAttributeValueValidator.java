package org.apereo.cas.attributes;

import java.util.List;
import java.util.Map;

import org.apereo.cas.configuration.model.support.attributes.EditableAttributeProperties.EditableAttribute;

/**
 * Provides an individual attribute validation method.
 * 
 * @author Marcus Watkins
 * @since 5.2.0
 *
 */
public abstract class AbstractEditableAttributeValueValidator implements EditableAttributeValueValidator {

    @Override
    public boolean areAttributeValuesValid(final List<EditableAttribute> attributes, final Map<String, String> attributeValues) {
        for (final EditableAttribute attr : attributes) {
            if (!isAttributeValueValid(attr, attributeValues.get(attr.getId()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Allows checking attributes one by one.
     * 
     * @param attribute Attribute settings
     * @param attributeValue Attribute value
     * @return Whether attribute value is valid for given settings
     */
    protected abstract boolean isAttributeValueValid(EditableAttribute attribute, String attributeValue);

}
