package org.apereo.cas.attributes;

import java.util.List;
import java.util.Map;

import org.apereo.cas.configuration.model.support.attributes.EditableAttributeProperties.EditableAttribute;

public abstract class AbstractEditableAttributeValueValidator implements EditableAttributeValueValidator {

	@Override
	public boolean areAttributeValuesValid(List<EditableAttribute> attributes, Map<String, String> attributeValues) {
		for(EditableAttribute attr : attributes) {
			if( !isAttributeValueValid(attr, attributeValues.get(attr.getId()))) {
				return false;
			}
		}
		return true;
	}
	abstract public boolean isAttributeValueValid(EditableAttribute attribute, String attributeValue);

}
