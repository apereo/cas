package org.apereo.cas.web.flow.login;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.decorator.GroovyLoginWebflowDecorator;
import org.apereo.cas.web.flow.decorator.RestfulLoginWebflowDecorator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link RenderLoginAction}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class RenderLoginAction extends AbstractAction {
    /**
     * The services manager with access to the registry.
     **/
    protected final ServicesManager servicesManager;

    /**
     * Collection of CAS settings.
     */
    protected final CasConfigurationProperties casProperties;

    /**
     * The current application context.
     */
    protected final ApplicationContext applicationContext;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val decorator = casProperties.getWebflow().getLoginDecorator();

        val groovyScript = decorator.getGroovy().getLocation();
        if (groovyScript != null) {
            LOGGER.trace("Decorating login webflow using [{}]", groovyScript);
            val groovy = new GroovyLoginWebflowDecorator(groovyScript);
            groovy.decorate(requestContext, applicationContext);
            return null;
        }

        val url = decorator.getRest().getUrl();
        if (StringUtils.isNotBlank(url)) {
            LOGGER.trace("Decorating login webflow REST endpoint [{}]", url);
            val rest = new RestfulLoginWebflowDecorator(decorator.getRest());
            rest.decorate(requestContext, applicationContext);
            return null;
        }

        return null;
    }
}
