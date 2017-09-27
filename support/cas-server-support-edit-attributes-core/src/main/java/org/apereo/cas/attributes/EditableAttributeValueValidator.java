package org.apereo.cas.attributes;

import java.util.List;
import java.util.Map;

import org.apereo.cas.configuration.model.support.attributes.EditableAttributeProperties.EditableAttribute;

public interface EditableAttributeValueValidator {

	public boolean areAttributeValuesValid(List<EditableAttribute> attributes, Map<String,String> attributeValues);
	
}
