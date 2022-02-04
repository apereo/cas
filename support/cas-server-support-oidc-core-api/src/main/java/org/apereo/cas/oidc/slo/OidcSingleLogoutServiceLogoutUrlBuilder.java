package org.apereo.cas.oidc.slo;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.logout.slo.BaseSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.web.UrlValidator;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link OidcSingleLogoutServiceLogoutUrlBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class OidcSingleLogoutServiceLogoutUrlBuilder extends BaseSingleLogoutServiceLogoutUrlBuilder {
    public OidcSingleLogoutServiceLogoutUrlBuilder(final ServicesManager servicesManager,
                                                   final UrlValidator urlValidator) {
        super(servicesManager, urlValidator);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public boolean supports(final RegisteredService registeredService,
                            final WebApplicationService singleLogoutService,
                            final Optional<HttpServletRequest> httpRequest) {
        return super.supports(registeredService, singleLogoutService, httpRequest)
               && registeredService instanceof OidcRegisteredService;
    }

    @Override
    public boolean isServiceAuthorized(final WebApplicationService service,
                                       final Optional<HttpServletRequest> requestOpt,
                                       final Optional<HttpServletResponse> response) {
        return requestOpt.map(request -> {
            val webContext = new JEEContext(request, response.get());
            val clientId = OAuth20Utils.getRequestParameter(webContext, OAuth20Constants.CLIENT_ID).orElse(StringUtils.EMPTY);
            if (StringUtils.isNotBlank(clientId)) {
                val foundService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
                return supports(foundService, service, requestOpt);
            }
            return false;
        }).orElse(Boolean.FALSE);
    }
}
