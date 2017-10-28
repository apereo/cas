package org.apereo.cas.mgmt.authentication;

import org.apereo.cas.CasProtocolConstants;
import org.pac4j.core.client.Client;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.Pac4jConstants;
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
import java.util.stream.Collectors;

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
        super(config, getClientNames(config), getAuthorizerNames(config));
        final CasManagementSecurityLogic logic = new CasManagementSecurityLogic();
        logic.setSaveProfileInSession(true);
        setSecurityLogic(logic);
    }

    @Override
    public void postHandle(final HttpServletRequest request, final HttpServletResponse response,
                           final Object handler, final ModelAndView modelAndView) {
        if (!StringUtils.isEmpty(request.getQueryString()) && request.getQueryString().contains(CasProtocolConstants.PARAMETER_TICKET)) {
            final RedirectView v = new RedirectView(request.getRequestURL().toString());
            v.setExposeModelAttributes(false);
            v.setExposePathVariables(false);
            modelAndView.setView(v);
        }
    }

    private static String getClientNames(final Config config) {
        return config.getClients().getClients().stream().map(Client::getName).collect(Collectors.joining(Pac4jConstants.ELEMENT_SEPRATOR));
    }

    private static String getAuthorizerNames(final Config config) {
        return config.getAuthorizers().keySet().stream().collect(Collectors.joining(Pac4jConstants.ELEMENT_SEPRATOR));
    }

    /**
     * The Cas management security logic.
     */
    public static class CasManagementSecurityLogic extends DefaultSecurityLogic {
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
