package org.apereo.cas.mgmt.web;

import org.apereo.cas.CasProtocolConstants;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.springframework.web.SecurityInterceptor;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * This is {@link CasManagementSecurityInterceptor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class CasManagementSecurityInterceptor extends SecurityInterceptor {

    /**
     * Instantiates a new Cas management security interceptor.
     *
     * @param config the config
     */
    public CasManagementSecurityInterceptor(final Config config) {
        super(config, "CasClient", "securityHeaders,csrfToken,RequireAnyRoleAuthorizer");
        final CasManagementSecurityLogic logic = new CasManagementSecurityLogic();
        logic.setSaveProfileInSession(true);
        setSecurityLogic(logic);
    }

    @Override
    public void postHandle(final HttpServletRequest request, final HttpServletResponse response,
                           final Object handler, final ModelAndView modelAndView) throws Exception {
        if (!StringUtils.isEmpty(request.getQueryString())
                && request.getQueryString().contains(CasProtocolConstants.PARAMETER_TICKET)) {
            final RedirectView v = new RedirectView(request.getRequestURL().toString());
            v.setExposeModelAttributes(false);
            v.setExposePathVariables(false);
            modelAndView.setView(v);
        }
    }

    /**
     * The Cas management security logic.
     */
    public class CasManagementSecurityLogic extends DefaultSecurityLogic {
        @Override
        protected HttpAction forbidden(final WebContext context, final List currentClients, final List list, final String authorizers) {
            return HttpAction.redirect("Authorization failed", context, "authorizationFailure");
        }

        @Override
        protected boolean loadProfilesFromSession(final WebContext context, final List currentClients) {
            return true;
        }
    }
}
