package org.apereo.cas.web.flow.actions.storage;

import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.DefaultBrowserStorage;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Map;

/**
 * This is {@link ReadBrowserStorageAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
public class ReadBrowserStorageAction extends BaseBrowserStorageAction {
    private String nextTransition = CasWebflowConstants.TRANSITION_ID_READ_BROWSER_STORAGE;

    public ReadBrowserStorageAction(final CasCookieBuilder ticketGrantingCookieBuilder) {
        super(ticketGrantingCookieBuilder);
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val currentEvent = requestContext.getCurrentEvent();
        if (currentEvent != null
            && hasFinishedReadingBrowserStorage(requestContext)
            && requestContext.getRequestParameters().contains(BrowserStorage.PARAMETER_BROWSER_STORAGE)) {
            val storageData = requestContext.getRequestParameters().getRequired(BrowserStorage.PARAMETER_BROWSER_STORAGE);
            if (StringUtils.isNotBlank(storageData)) {
                val storageMap = MAPPER.readValue(storageData, Map.class);
                return hydrateWebflowFromStorage(storageMap, requestContext);
            }
            return null;
        }

        val browserStorage = new DefaultBrowserStorage()
            .setStorageType(determineStorageType(requestContext))
            .setContext(browserStorageContextKey)
            .setRemoveOnRead(false);
        requestContext.getFlowScope().put(BrowserStorage.PARAMETER_BROWSER_STORAGE, browserStorage);
        return result(nextTransition);
    }

    private static boolean hasFinishedReadingBrowserStorage(final RequestContext requestContext) {
        val targetEventId = requestContext.getFlowScope().get("targetEventId", String.class, CasWebflowConstants.TRANSITION_ID_CONTINUE);
        return requestContext.getCurrentEvent().getId().equals(targetEventId);
    }

    protected Event hydrateWebflowFromStorage(final Map<String, String> storageMap, final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val cookieValue = storageMap.get(ticketGrantingCookieBuilder.getCookieName());
        val ticketGrantingTicketId = ticketGrantingCookieBuilder.getCasCookieValueManager().obtainCookieValue(cookieValue, request);
        WebUtils.putTicketGrantingTicketInScopes(requestContext, ticketGrantingTicketId);
        return null;
    }
}
