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
    public ReadBrowserStorageAction(final CasCookieBuilder ticketGrantingCookieBuilder) {
        super(ticketGrantingCookieBuilder);
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val browserStorage = new DefaultBrowserStorage()
            .setStorageType(determineStorageType(requestContext))
            .setContext(WebUtils.getBrowserStorageContextKey(requestContext, this.browserStorageContextKey))
            .setRemoveOnRead(false);
        
        val storageResult = WebUtils.getBrowserStoragePayload(requestContext);
        if (storageResult.isPresent()) {
            val storageData = storageResult.get();
            browserStorage.setPayload(Objects.requireNonNull(storageData));
            hydrateWebflowFromStorage(browserStorage, requestContext);
            return result(CasWebflowConstants.TRANSITION_ID_SUCCESS, BrowserStorage.PARAMETER_BROWSER_STORAGE, browserStorage);
        }
        WebUtils.putBrowserStorage(requestContext, browserStorage);
        return error();
    }


    protected void hydrateWebflowFromStorage(final BrowserStorage browserStorage, final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val storageMap = (Map<String, String>) browserStorage.getPayloadJson(Map.class);
        if (storageMap.containsKey(ticketGrantingCookieBuilder.getCookieName())) {
            val cookieValue = storageMap.get(ticketGrantingCookieBuilder.getCookieName());
            val ticketGrantingTicketId = ticketGrantingCookieBuilder.getCasCookieValueManager().obtainCookieValue(cookieValue, request);
            WebUtils.putTicketGrantingTicketInScopes(requestContext, ticketGrantingTicketId);
        }
        WebUtils.putBrowserStorage(requestContext, browserStorage);
    }

}
