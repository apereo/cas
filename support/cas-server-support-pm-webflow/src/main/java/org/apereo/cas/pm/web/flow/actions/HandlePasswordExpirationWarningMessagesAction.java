package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.support.password.PasswordExpiringWarningMessageDescriptor;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
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
public class HandlePasswordExpirationWarningMessagesAction extends BaseCasWebflowAction {
    /**
     * Attribute indicating password expiration warnings are found.
     */
    public static final String ATTRIBUTE_NAME_EXPIRATION_WARNING_FOUND = "passwordExpirationWarningFound";

    @Override
    protected Event doExecuteInternal(final RequestContext context) {
        val attributes = context.getCurrentEvent().getAttributes();
        val warnings = (Collection<MessageDescriptor>)
            attributes.get(CasWebflowConstants.ATTRIBUTE_ID_AUTHENTICATION_WARNINGS, Collection.class, new LinkedHashSet<>());
        val found = warnings
            .stream()
            .filter(PasswordExpiringWarningMessageDescriptor.class::isInstance)
            .findAny();
        context.getFlowScope().put(ATTRIBUTE_NAME_EXPIRATION_WARNING_FOUND, found.isPresent());
        Optional.ofNullable(WebUtils.getCredential(context, Credential.class))
                .ifPresent(upc -> PasswordManagementWebflowUtils.putPasswordResetUsername(context, upc.getId()));
        return null;
    }
}
