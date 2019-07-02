package org.apereo.cas.web.flow.actions;

import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Performs two important error handling functions on an
 * {@link org.apereo.cas.authentication.AuthenticationException} raised from the authentication
 * layer:
 * <ol>
 * <li>Maps handler errors onto message bundle strings for display to user.</li>
 * <li>Determines the next webflow state by comparing handler errors.
 * in list order. The first entry that matches determines the outcome state, which
 * is the simple class name of the exception.</li>
 * </ol>
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class AuthenticationExceptionHandlerAction extends AbstractAction {
    private final List<CasWebflowExceptionHandler> webflowExceptionHandlers;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val currentEvent = requestContext.getCurrentEvent();
        LOGGER.debug("Located current event [{}]", currentEvent);

        val error = currentEvent.getAttributes().get(CasWebflowConstants.TRANSITION_ID_ERROR, Exception.class);
        if (error != null) {
            LOGGER.debug("Located error attribute [{}] with message [{}] from the current event", error.getClass(), error.getMessage());

            val event = handle(error, requestContext);
            LOGGER.debug("Final event id resolved from the error is [{}]", event);
            return new EventFactorySupport().event(this, event, currentEvent.getAttributes());
        }
        return error();
    }

    /**
     * Maps an authentication exception onto a state name.
     * Also sets an ERROR severity message in the message context.
     *
     * @param e              Authentication error to handle.
     * @param requestContext the spring  context
     * @return Name of next flow state to transition to or {@value CasWebflowExceptionHandler#UNKNOWN}
     */
    public String handle(final Exception e, final RequestContext requestContext) {
        val handlers = webflowExceptionHandlers
            .stream()
            .filter(handler -> handler.supports(e, requestContext))
            .collect(Collectors.toList());

        return handlers
            .stream()
            .map(handler -> handler.handle(e, requestContext))
            .filter(Objects::nonNull)
            .findFirst()
            .orElseGet(this::error)
            .getId();
    }
}
