package org.apereo.cas.web.pac4j;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.springframework.web.SecurityInterceptor;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This is {@link CasSecurityInterceptor}.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
public class CasSecurityInterceptor extends SecurityInterceptor {

    public CasSecurityInterceptor(final Config config, final String clients) {
        super(config, clients);
    }

    public CasSecurityInterceptor(final Config config, final String clients,
            final String authorizers) {

        super(config, clients, authorizers);

        final DefaultSecurityLogic secLogic = new DefaultSecurityLogic() {
            @Override
            protected HttpAction unauthorized(final WebContext context, final List currentClients) {
                return HttpAction.forbidden("Access Denied", context);
            }

            @Override
            protected boolean loadProfilesFromSession(final WebContext context, final List currentClients) {
                return true;
            }
        };
        secLogic.setSaveProfileInSession(true);
        setSecurityLogic(secLogic);
    }

    @Override
    public void postHandle(final HttpServletRequest request, final HttpServletResponse response,
                           final Object handler, final ModelAndView modelAndView) {
        if (modelAndView != null
                && StringUtils.isNotBlank(request.getQueryString())
                && request.getQueryString().contains(CasProtocolConstants.PARAMETER_TICKET)) {
            final RedirectView v = new RedirectView(request.getRequestURL().toString());
            v.setExposeModelAttributes(false);
            v.setExposePathVariables(false);
            modelAndView.setView(v);
        }
    }
}
