package org.apereo.cas.attributes;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.attributes.EditableAttributeProperties;
import org.apereo.cas.configuration.model.support.attributes.EditableAttributeProperties.EditableAttribute;
import org.springframework.webflow.execution.RequestContext;

public class DefaultEditableAttributeRepository extends AbstractEditableAttributeRepository {

	private static final long serialVersionUID = 476159832176564L;

	private EditableAttributeProperties editableAttributeProperties;
	private EditableAttributeValueValidator validator;
	
	public DefaultEditableAttributeRepository(EditableAttributeProperties editableAttributeProperties, EditableAttributeValueValidator validator) {
		this.editableAttributeProperties = editableAttributeProperties;
		this.validator = validator;
	}
	
	@Override
	public List<EditableAttribute> getAttributes(RequestContext requestContext, Credential credential) {
		return editableAttributeProperties.getEditableAttributes();
	}

	@Override
	public boolean isAttributeValueNeeded(RequestContext requestContext, Credential credential,
			Map<String, String> attributeValues) {
		
		// TODO Auto-generated method stub
		return false;
	}

}
