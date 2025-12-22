package org.apereo.cas.web.flow.login;

import module java.base;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link RedirectUnauthorizedServiceUrlAction}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class RedirectUnauthorizedServiceUrlAction extends BaseCasWebflowAction {
    private final ServicesManager servicesManager;

    private final ObjectProvider<@NonNull ScriptResourceCacheManager> scriptResourceCacheManager;
    
    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        var redirectUrl = determineUnauthorizedServiceRedirectUrl(requestContext);
        if (redirectUrl != null) {
            val url = redirectUrl.toString();
            val scriptFactory = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();
            if (scriptFactory.isPresent() && scriptFactory.get().isScript(url)) {
                redirectUrl = FunctionUtils.doUnchecked(() -> {
                    val registeredService = WebUtils.getRegisteredService(requestContext);
                    val applicationContext = requestContext.getActiveFlow().getApplicationContext();
                    val authentication = WebUtils.getAuthentication(requestContext);
                    val args = CollectionUtils.<String, Object>wrap("registeredService", registeredService,
                        "authentication", authentication,
                        "requestContext", requestContext,
                        "applicationContext", applicationContext,
                        "logger", LOGGER);
                    val scriptToExec = scriptResourceCacheManager.getObject().resolveScriptableResource(url);
                    scriptToExec.setBinding(args);
                    return scriptToExec.execute(args.values().toArray(), URI.class);
                });
            }
            LOGGER.debug("Redirecting to unauthorized redirect URL [{}]", redirectUrl);
            WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(requestContext, redirectUrl);
        }
        return null;
    }

    protected URI determineUnauthorizedServiceRedirectUrl(final RequestContext context) {
        val redirectUrl = WebUtils.getUnauthorizedRedirectUrlFromFlowScope(context);
        val currentEvent = context.getCurrentEvent();
        if (currentEvent != null) {
            val eventAttributes = currentEvent.getAttributes();
            LOGGER.debug("Finalizing the unauthorized redirect URL [{}] when processing event [{}] with attributes [{}]",
                redirectUrl, currentEvent.getId(), eventAttributes);
        }
        return redirectUrl;
    }
}
