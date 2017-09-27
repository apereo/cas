package org.apereo.cas.attributes;

import org.apereo.cas.configuration.model.support.attributes.EditableAttributeProperties.EditableAttribute;

public class DefaultEditableAttributeValueValidator {

	protected boolean isAttributeValueValid(EditableAttribute attribute, String attributeValue) {
		if( attributeValue == null ) {
			attributeValue = "";
		}
		String validRegex = attribute.getValidationRegex();
		if( validRegex != null ) {
			return attributeValue.matches(validRegex);
		}
		return true;
	}

}
