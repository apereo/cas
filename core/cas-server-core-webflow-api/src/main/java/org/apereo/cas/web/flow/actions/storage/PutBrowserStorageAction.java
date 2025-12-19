package org.apereo.cas.web.flow.actions.storage;

import module java.base;
import org.apereo.cas.web.DefaultBrowserStorage;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PutBrowserStorageAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
@Slf4j
public class PutBrowserStorageAction extends BaseBrowserStorageAction {
    public PutBrowserStorageAction(final CasCookieBuilder ticketGrantingCookieBuilder) {
        super(ticketGrantingCookieBuilder);
    }

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val browserStorage = new DefaultBrowserStorage()
            .setStorageType(determineStorageType(requestContext))
            .setContext(WebUtils.getBrowserStorageContextKey(requestContext, this.browserStorageContextKey));
        WebUtils.putBrowserStorage(requestContext, browserStorage);
        return null;
    }
}
