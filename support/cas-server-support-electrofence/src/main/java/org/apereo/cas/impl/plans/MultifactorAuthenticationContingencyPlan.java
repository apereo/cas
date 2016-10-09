package org.apereo.cas.impl.plans;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.api.AuthenticationRiskContingencyResponse;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.ApplicationContextProvider;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.execution.Event;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * This is {@link MultifactorAuthenticationContingencyPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class MultifactorAuthenticationContingencyPlan extends BaseAuthenticationRiskContingencyPlan {
    
    @Override
    protected AuthenticationRiskContingencyResponse executeInternal(final Authentication authentication, 
                                                                    final RegisteredService service,
                                                                    final AuthenticationRiskScore score, 
                                                                    final HttpServletRequest request) {
        final String id = casProperties.getAuthn().getAdaptive().getRisk().getResponse().getMfaProvider();
        if (StringUtils.isBlank(id)) {
            logger.warn("No multifactor authentication providers are specified to handle risk-based authentication");
            throw new AuthenticationException();
        }

        final ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        final Map<String, MultifactorAuthenticationProvider> providerMap =
                WebUtils.getAvailableMultifactorAuthenticationProviders(applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            logger.warn("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }
        return new AuthenticationRiskContingencyResponse(new Event(this, id));
    }
}
