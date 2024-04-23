package org.apereo.cas.support.oauth.web.views;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import org.pac4j.core.context.WebContext;
import org.springframework.web.servlet.ModelAndView;

/**
 * This is {@link ConsentApprovalViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface ConsentApprovalViewResolver {

    /**
     * Resolve model and view.
     *
     * @param context the context
     * @param service the service
     * @return the model and view. Could be an empty view which would indicate consent is not required.
     * @throws Exception the exception
     */
    ModelAndView resolve(WebContext context, OAuthRegisteredService service) throws Exception;
}
