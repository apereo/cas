package org.apereo.cas.attributes;

import java.util.List;

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
public class DefaultEditableAttributeRepository implements EditableAttributeRepository {

    private static final long serialVersionUID = 476159832176564L;

    private EditableAttributeProperties editableAttributeProperties;

    public DefaultEditableAttributeRepository(final EditableAttributeProperties editableAttributeProperties) {
        this.editableAttributeProperties = editableAttributeProperties;
    }

    @Override
    public List<EditableAttribute> getAttributes(final RequestContext requestContext, final Credential credential) {
        return editableAttributeProperties.getAttributes();
    }

}
