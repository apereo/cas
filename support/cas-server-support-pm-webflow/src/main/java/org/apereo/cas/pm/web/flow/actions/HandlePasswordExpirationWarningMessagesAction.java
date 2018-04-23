package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.support.password.PasswordExpiringWarningMessageDescriptor;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

/**
 * This is {@link HandlePasswordExpirationWarningMessagesAction}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class HandlePasswordExpirationWarningMessagesAction extends AbstractAction {
    @Override
    protected Event doPreExecute(final RequestContext context) throws Exception {
        return super.doPreExecute(context);
    }

    @Override
    protected Event doExecute(final RequestContext context) {
        final AttributeMap attributes = context.getCurrentEvent().getAttributes();
        final Collection<MessageDescriptor> warnings = (Collection<MessageDescriptor>)
            attributes.get(CasWebflowConstants.ATTRIBUTE_ID_AUTHENTICATION_WARNINGS, Collection.class, new LinkedHashSet<>());
        final Optional<MessageDescriptor> found = warnings
            .stream()
            .filter(PasswordExpiringWarningMessageDescriptor.class::isInstance)
            .findAny();
        context.getFlowScope().put("passwordExpirationWarningFound", found.isPresent());
        return null;
    }
}
