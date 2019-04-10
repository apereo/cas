package org.apereo.cas.web.flow.login;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.net.URI;

/**
 * This is {@link RedirectUnauthorizedServiceUrlAction}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class RedirectUnauthorizedServiceUrlAction extends AbstractAction {
    private final ServicesManager servicesManager;
    private final ResourceLoader resourceLoader;
    private final ApplicationContext applicationContext;

    @Override
    public Event doExecute(final RequestContext context) {
        var redirectUrl = determineUnauthorizedServiceRedirectUrl(context);

        val url = redirectUrl.toString();
        if (ScriptingUtils.isExternalGroovyScript(url)) {
            val scriptResource = this.resourceLoader.getResource(url);
            val registeredService = WebUtils.getRegisteredService(context);
            val args = new Object[]{registeredService, context, this.applicationContext, LOGGER};
            redirectUrl = ScriptingUtils.executeGroovyScript(scriptResource, args, URI.class, true);
        }

        LOGGER.debug("Redirecting to unauthorized redirect URL [{}]", redirectUrl);
        WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context, redirectUrl);
        return null;
    }

    /**
     * Determine unauthorized service redirect url.
     *
     * @param context the context
     * @return the uri
     */
    protected URI determineUnauthorizedServiceRedirectUrl(final RequestContext context) {
        val redirectUrl = WebUtils.getUnauthorizedRedirectUrlFromFlowScope(context);
        val currentEvent = context.getCurrentEvent();
        val eventAttributes = currentEvent.getAttributes();
        LOGGER.debug("Finalizing the unauthorized redirect URL [{}] when processing event [{}] with attributes [{}]",
            redirectUrl, currentEvent.getId(), eventAttributes);
        return redirectUrl;
    }
}
