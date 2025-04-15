package org.apereo.cas.web.flow.actions.storage;

import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.DefaultBrowserStorage;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link ReadBrowserStorageAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
@Slf4j
public class ReadBrowserStorageAction extends BaseBrowserStorageAction {
    /**
     * Flag to indicate the read request is in progress and might be looping back.
     */
    public static final String BROWSER_STORAGE_REQUEST_IN_PROGRESS = "BrowserStorageReadRequest";

    public ReadBrowserStorageAction(final CasCookieBuilder ticketGrantingCookieBuilder) {
        super(ticketGrantingCookieBuilder);
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val browserStorage = new DefaultBrowserStorage()
            .setContext(WebUtils.getBrowserStorageContextKey(requestContext, this.browserStorageContextKey))
            .setStorageType(determineStorageType(requestContext));
        val storageResult = WebUtils.getBrowserStoragePayload(requestContext);
        if (storageResult.isPresent()) {
            val storageData = storageResult.get();
            browserStorage.setPayload(Objects.requireNonNull(storageData));
            hydrateWebflowFromStorage(browserStorage, requestContext);

            val targetState = WebUtils.getTargetState(requestContext);
            if (StringUtils.isBlank(targetState)) {
                WebUtils.putTargetState(requestContext, CasWebflowConstants.STATE_ID_TICKET_GRANTING_TICKET_CHECK);
            }
            requestContext.getFlowScope().remove(BROWSER_STORAGE_REQUEST_IN_PROGRESS);
            return result(CasWebflowConstants.TRANSITION_ID_SUCCESS, BrowserStorage.PARAMETER_BROWSER_STORAGE, browserStorage);
        }
        if (requestContext.getFlowScope().contains(BROWSER_STORAGE_REQUEST_IN_PROGRESS)) {
            requestContext.getFlowScope().remove(BROWSER_STORAGE_REQUEST_IN_PROGRESS);
            return result(CasWebflowConstants.TRANSITION_ID_SKIP);
        }
        WebUtils.putBrowserStorage(requestContext, browserStorage);
        requestContext.getFlowScope().put(BROWSER_STORAGE_REQUEST_IN_PROGRESS, Boolean.TRUE);
        return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_READ_BROWSER_STORAGE);
    }


    protected void hydrateWebflowFromStorage(final BrowserStorage browserStorage, final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val storageMap = (Map) browserStorage.getPayloadJson();
        if (storageMap != null && storageMap.containsKey(ticketGrantingCookieBuilder.getCookieName())) {
            val cookieValue = storageMap.get(ticketGrantingCookieBuilder.getCookieName()).toString();
            val ticketGrantingTicketId = ticketGrantingCookieBuilder.getCasCookieValueManager().obtainCookieValue(cookieValue, request);
            WebUtils.putTicketGrantingTicketInScopes(requestContext, ticketGrantingTicketId);
        }
        WebUtils.putBrowserStorage(requestContext, browserStorage);
    }
}
