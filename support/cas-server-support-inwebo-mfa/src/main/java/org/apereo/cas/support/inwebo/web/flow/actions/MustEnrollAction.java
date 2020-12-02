package org.apereo.cas.support.inwebo.web.flow.actions;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * A web action to enable the enrollment.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@RequiredArgsConstructor
public class MustEnrollAction extends AbstractAction implements WebflowConstants {

    private final MessageSource messageSource;

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val flowScope = requestContext.getFlowScope();
        flowScope.put(MUST_ENROLL, true);
        flowScope.put(INWEBO_ERROR_MESSAGE, messageSource.getMessage("cas.inwebo.error.usernotregistered", null, LocaleContextHolder.getLocale()));
        return success();
    }
}
