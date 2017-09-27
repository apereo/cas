package org.apereo.cas.attributes;

import org.apereo.cas.configuration.model.support.attributes.EditableAttributeProperties.EditableAttribute;

/**
 * Validates attribute values based on regex from properties.
 * 
 * @author Marcus Watkins
 * @since 5.2
 *
 */
public class DefaultEditableAttributeValueValidator extends AbstractEditableAttributeValueValidator {

    /**
     * Checks whether attribute matches regex from properties file.
     * 
     * @param attribute Attribute
     * @param attributeValue Value of attribute to test
     * @return whether attribute value matches attributes allowable regex
     */
    protected boolean isAttributeValueValid(final EditableAttribute attribute, final String attributeValue) {
        final String testValue = attributeValue == null ? "" : attributeValue;
        final String validRegex = attribute.getValidationRegex();
        if (validRegex != null) {
            return testValue.matches(validRegex);
        }
        return true;
    }

}
