package org.apereo.cas.oidc.slo;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.logout.slo.BaseSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * This is {@link OidcSingleLogoutServiceLogoutUrlBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class OidcSingleLogoutServiceLogoutUrlBuilder extends BaseSingleLogoutServiceLogoutUrlBuilder {
    public OidcSingleLogoutServiceLogoutUrlBuilder(final ServicesManager servicesManager) {
        super(servicesManager);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public boolean isServiceAuthorized(final WebApplicationService service,
                                       final Optional<HttpServletRequest> requestOpt) {
        if (requestOpt.isPresent()) {
            val request = requestOpt.get();
            val clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
            if (StringUtils.isNotBlank(clientId)) {
                val foundService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
                return supports(foundService, service, requestOpt);
            }
        }
        return false;
    }
}
