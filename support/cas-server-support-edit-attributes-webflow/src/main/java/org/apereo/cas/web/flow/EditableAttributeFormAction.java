package org.apereo.cas.web.flow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.attributes.EditableAttributeRepository;
import org.apereo.cas.attributes.EditableAttributeValueRepository;
import org.apereo.cas.attributes.EditableAttributeValueValidator;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.attributes.EditableAttributeProperties.EditableAttribute;
import org.apereo.cas.configuration.model.support.attributes.EditableAttributeProperties.EditableAttribute.EditableAttributeType;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Webflow action to receive and record the editable attribute values.
 *
 * @author Marcus Watkins
 * @since 5.2
 */
public class EditableAttributeFormAction extends AbstractAction {

    /**
     * Event id to signal attributes are incomplete or invalid.
     **/
    protected static final String EVENT_ID_ATTRIBUTE_VALUES_NEEDED = "attributesNeeded";
    private static final String KEEP_SECRET_VALUE = "x4W3$AQQlo3GXmJZUPd!";

    public static final String FLOW_STORAGE_ATTRIBUTES = "editableAttributes";
    public static final String FLOW_STORAGE_EXISTING_VALUES = "existingEditableAttributeValues";
    public static final String FLOW_STORAGE_INPUT_VALUES = "inputEditableAttributeValues";
    public static final String FLOW_STORAGE_OUTPUT_VALUES = "outputEditableAttributeValues";
    public static final String FLOW_STORAGE_PRINCIPAL = "principal";
    
    public static final String ATTRIBUTE_INPUT_PREFIX = "attr:";
    
    private final EditableAttributeValueRepository valueRepository;
    private final EditableAttributeRepository attributeRepository;
    private final EditableAttributeValueValidator validator;

    public EditableAttributeFormAction(final EditableAttributeValueRepository valueRepository, 
    		final EditableAttributeRepository attributeRepository,
    		EditableAttributeValueValidator validator) {
        this.valueRepository = valueRepository;
        this.attributeRepository = attributeRepository;
        this.validator = validator;
    }

    /**
     * Verify whether all attributes are valid.
     *
     * @param context        the context
     * @param credential     the credential
     * @param messageContext the message context
     * @return success if attributes are valid. {@link #EVENT_ID_ATTRIBUTE_VALUES_NEEDED} otherwise.
     */
    public Event verify(final RequestContext requestContext, final Credential credential, final MessageContext messageContext) {
    	populateFlowScope(requestContext, credential);
    	
    	@SuppressWarnings("unchecked")
		Map<String,String> existingValues = (Map<String, String>) requestContext.getFlowScope().get(FLOW_STORAGE_EXISTING_VALUES);
    	
    	@SuppressWarnings("unchecked")
		List<EditableAttribute> attributes = (List<EditableAttribute>) requestContext.getFlowScope().get(FLOW_STORAGE_ATTRIBUTES);
    	
    	
    	if( validator.areAttributeValuesValid(attributes, existingValues ) ) {
    		return success();
    	}
        return gather();
    }

    /**
     * Record attribute values.
     *
     * @param context        the context
     * @param credential     the credential
     * @param messageContext the message context
     * @return success if editable attributes are recorded successfully.
     */
    public Event submit(final RequestContext requestContext, final Credential credential, final MessageContext messageContext) {
    	
    	populateFlowScope(requestContext, credential);
    	
    	@SuppressWarnings("unchecked")
		Map<String,String> inputValues = (Map<String, String>) requestContext.getFlowScope().get(FLOW_STORAGE_INPUT_VALUES);
    	
    	@SuppressWarnings("unchecked")
		List<EditableAttribute> attributes = (List<EditableAttribute>) requestContext.getFlowScope().get(FLOW_STORAGE_ATTRIBUTES);

    	if( validator.areAttributeValuesValid(attributes, inputValues) ) {
        	if( valueRepository.storeAttributeValues(requestContext, credential, inputValues) ) {
        		return success();
        	}
        }
        //TODO: Report reasons here

        return error(); //TODO: return gather?
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
    	return verify(requestContext, WebUtils.getCredential(requestContext), requestContext.getMessageContext());
    }

    /**
     * Attributes needed signaled by id {@link #EVENT_ID_ATTRIBUTE_VALUES_NEEDED}.
     *
     * @return the event
     */
    protected Event gather() {
        return new EventFactorySupport().event(this, EVENT_ID_ATTRIBUTE_VALUES_NEEDED);
    }
    
    
    /**
     * Utility method to populate all possible flow variables to avoid duplication in other methods.
     * 
     * @param requestContext Request context
     * @param credential optional credential (will be fetched if null)
     */
    private void populateFlowScope(final RequestContext requestContext, Credential credential ) {
    	if( credential == null ) {
    		credential = WebUtils.getCredential(requestContext);
    	}
        final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);

        List<EditableAttribute> attributes = attributeRepository.getAttributes(requestContext, credential);
    	requestContext.getFlowScope().put(FLOW_STORAGE_ATTRIBUTES, attributes);
    	
    	Pair<Principal, Map<String, String>> principalAndValues = valueRepository.getAttributeValues(requestContext, credential, attributes.stream().map(
    			attribute -> attribute.getId() ).collect(Collectors.<String> toSet() ));
    	requestContext.getFlowScope().put(FLOW_STORAGE_PRINCIPAL, principalAndValues.getLeft() );
    	
    	Map<String,String> existingValues = principalAndValues.getRight();
    	requestContext.getFlowScope().put(FLOW_STORAGE_EXISTING_VALUES, existingValues );
    	requestContext.getFlowScope().put(FLOW_STORAGE_OUTPUT_VALUES, sanitizeOutputValues(attributes, existingValues));
    	
        requestContext.getFlowScope().put(FLOW_STORAGE_INPUT_VALUES, processInputValues(attributes, existingValues, request));
    	
    }
    
    /**
     * Process input values so replacing placeholders with proper existing value.
     * 
     * @param attributes List of attributes
     * @param existingValues Existing attribute values
     * @param request request containing form values
     * @return actual values containing secret data and no placeholders
     */
    private static Map<String,String> processInputValues( List<EditableAttribute> attributes,
    		Map<String,String> existingValues, final HttpServletRequest request ) {
        HashMap<String,String> inputValues = new HashMap<>();
        attributes.forEach( attr -> {
        	String value = request.getParameter(ATTRIBUTE_INPUT_PREFIX + attr.getId() );
        	if( attr.getType() == EditableAttributeType.PASSWORD && KEEP_SECRET_VALUE.equals( value ) ) {
        		value = existingValues.get(attr.getId());
        	}
        	inputValues.put( attr.getId(), value );
        });
        return inputValues;
    }
    
    /**
     * Preserve secrecy of password inputs by replacing them with a known placeholder.
     * 
     * @param attributes Attribute list
     * @param existingValues Existing attribute values
     * @return output values with secrets replaced with a placeholder
     */
    private static Map<String,String> sanitizeOutputValues( List<EditableAttribute> attributes, Map<String,String> existingValues ) {
    	HashMap<String,String> outputValues = new HashMap<>();
    	attributes.forEach(attr -> {
    		String id = attr.getId();
    		String existingValue = existingValues.get(id);
    		if( !StringUtils.isBlank(existingValue) ) {
        		if( attr.getType() == EditableAttributeType.PASSWORD ) {
        			outputValues.put(attr.getId(), KEEP_SECRET_VALUE);
        		}
    			else {
    				outputValues.put(id, existingValue);
    			}
    		}
    	});
    	return outputValues;
    }
    
}
