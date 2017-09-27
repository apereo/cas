package org.apereo.cas.attributes;

import java.util.List;
import java.util.Map;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.attributes.EditableAttributeProperties;
import org.apereo.cas.configuration.model.support.attributes.EditableAttributeProperties.EditableAttribute;
import org.springframework.webflow.execution.RequestContext;

/**
 * Gets attributes from main CAS properties.
 * 
 * @author Marcus Watkins
 * @since 5.2.0
 *
 */
public class DefaultEditableAttributeRepository extends AbstractEditableAttributeRepository {

    private static final long serialVersionUID = 476159832176564L;

    private EditableAttributeProperties editableAttributeProperties;
    private EditableAttributeValueValidator validator;

    public DefaultEditableAttributeRepository(final EditableAttributeProperties editableAttributeProperties,
            final EditableAttributeValueValidator validator) {
        this.editableAttributeProperties = editableAttributeProperties;
        this.validator = validator;
    }

    @Override
    public List<EditableAttribute> getAttributes(final RequestContext requestContext, final Credential credential) {
        return editableAttributeProperties.getAttributes();
    }

    @Override
    public boolean isAttributeValueNeeded(final RequestContext requestContext, final Credential credential,
            final Map<String, String> attributeValues) {

        // TODO Auto-generated method stub
        return false;
    }

}
