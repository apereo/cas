package org.apereo.cas.impl.plans;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.api.AuthenticationRiskContingencyResponse;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(MultifactorAuthenticationContingencyPlan.class);
    
    @Override
    protected AuthenticationRiskContingencyResponse executeInternal(final Authentication authentication,
                                                                    final RegisteredService service,
                                                                    final AuthenticationRiskScore score,
                                                                    final HttpServletRequest request) {
        
        final Map<String, MultifactorAuthenticationProvider> providerMap =
                MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            LOGGER.warn("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }

        String id = casProperties.getAuthn().getAdaptive().getRisk().getResponse().getMfaProvider();
        if (StringUtils.isBlank(id)) {
            if (providerMap.size() == 1) {
                id = providerMap.values().iterator().next().getId();
            } else {
                LOGGER.warn("No multifactor authentication providers are specified to handle risk-based authentication");
                throw new AuthenticationException();
            }
        }

        final String attributeName = casProperties.getAuthn().getAdaptive().getRisk().getResponse().getRiskyAuthenticationAttribute();
        final Authentication newAuthn = DefaultAuthenticationBuilder.newInstance(authentication)
                .addAttribute(attributeName, Boolean.TRUE)
                .build();
        LOGGER.debug("Updated authentication to remember risk-based authn via [{}]", attributeName);
        authentication.update(newAuthn);
        return new AuthenticationRiskContingencyResponse(new Event(this, id));
    }
}
