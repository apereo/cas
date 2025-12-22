package org.apereo.cas.web.flow.actions;

import module java.base;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionCatalog;
import org.apereo.cas.web.flow.authentication.CasWebflowExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

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
public class AuthenticationExceptionHandlerAction extends BaseCasWebflowAction {
    private final List<CasWebflowExceptionHandler> webflowExceptionHandlers;

    /**
     * Maps an authentication exception onto a state name.
     * Also sets an ERROR severity message in the message context.
     *
     * @param exception      Authentication error to handle.
     * @param requestContext the spring  context
     * @return Name of next flow state to transition to or {@value CasWebflowExceptionCatalog#UNKNOWN}
     */
    protected Event handle(final Exception exception, final RequestContext requestContext) {
        val handlers = webflowExceptionHandlers
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .filter(Unchecked.predicate(handler -> handler.supports(exception, requestContext)))
            .toList();

        return handlers
            .stream()
            .map(Unchecked.function(handler -> handler.handle(exception, requestContext)))
            .filter(Objects::nonNull)
            .findFirst()
            .orElseGet(this::error);
    }

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val currentEvent = requestContext.getCurrentEvent();
        LOGGER.debug("Located current event [{}]", currentEvent);

        val error = currentEvent.getAttributes().get(CasWebflowConstants.TRANSITION_ID_ERROR, Exception.class);
        if (error != null) {
            LOGGER.debug("Located error attribute [{}] with message [{}] from the current event", error.getClass(), error.getMessage());
            val event = handle(error, requestContext);
            LOGGER.debug("Final event id resolved from the error is [{}]", event);
            return event;
        }
        return error();
    }
}
