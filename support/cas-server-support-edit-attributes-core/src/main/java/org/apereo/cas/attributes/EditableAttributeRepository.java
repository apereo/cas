package org.apereo.cas.attributes;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.attributes.EditableAttributeProperties.EditableAttribute;
import org.springframework.webflow.execution.RequestContext;

public interface EditableAttributeRepository extends Serializable {

    /**
     * Gather list of attributes.
     *
     * @param requestContext the request context
     * @param credential     the credential
     * @return list of attributes and the associated principal.
     */
	List<EditableAttribute> getAttributes(RequestContext requestContext, Credential credential);
	
    /**
     * Check if user is missing required attributes values.
     * 
     * @param requestContext the request context
     * @param credential     the credential
     * @return true if user needs to supply attributes.
     */
    boolean isAttributeValueNeeded(RequestContext requestContext, Credential credential, Map<String,String> attributeValues);
	
    
    
}
