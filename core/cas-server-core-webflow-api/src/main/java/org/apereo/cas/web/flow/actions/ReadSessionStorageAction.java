package org.apereo.cas.web.flow.actions;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.BrowserSessionStorage;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Map;

/**
 * This is {@link ReadSessionStorageAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Getter
public class ReadSessionStorageAction extends BaseCasWebflowAction {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).minimal(true).build().toObjectMapper();

    private final CasCookieBuilder ticketGrantingCookieBuilder;
    private final String sessionStorageContextKey;
    private final String nextTransition;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val currentEvent = requestContext.getCurrentEvent();
        if (currentEvent != null
            && currentEvent.getId().equals(CasWebflowConstants.TRANSITION_ID_CONTINUE)
            && requestContext.getRequestParameters().contains(BrowserSessionStorage.KEY_SESSION_STORAGE)) {
            val storageData = requestContext.getRequestParameters().getRequired(BrowserSessionStorage.KEY_SESSION_STORAGE);
            if (StringUtils.isNotBlank(storageData)) {
                val storageMap = MAPPER.readValue(storageData, Map.class);
                return hydrateWebflowFromStorage(storageMap, requestContext);
            }
            return null;
        }
        requestContext.getFlowScope().put(BrowserSessionStorage.KEY_SESSION_STORAGE_CONTEXT, sessionStorageContextKey);
        return result(nextTransition);
    }

    protected Event hydrateWebflowFromStorage(final Map<String, String> storageMap, final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val cookieValue = storageMap.get(ticketGrantingCookieBuilder.getCookieName());
        val ticketGrantingTicketId = ticketGrantingCookieBuilder.getCasCookieValueManager().obtainCookieValue(cookieValue, request);
        WebUtils.putTicketGrantingTicketInScopes(requestContext, ticketGrantingTicketId);
        return null;
    }
}
