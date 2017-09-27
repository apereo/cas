package org.apereo.cas.attributes;

import java.util.HashMap;
import java.util.Map;

import org.apereo.cas.authentication.Credential;
import org.springframework.webflow.execution.RequestContext;

public abstract class AbstractEditableAttributeRepository implements EditableAttributeRepository {

	public Map<String,String> readAttributeValues(RequestContext requestContext, Credential credential) {
		HashMap<String,String> attributeValues = new HashMap<>();
		
		
		
		return attributeValues;
	}
	
}
