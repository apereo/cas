package org.apereo.cas.impl.plans;

import module java.base;
import org.apereo.cas.api.AuthenticationRiskContingencyResponse;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderAbsentException;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.execution.Event;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link MultifactorAuthenticationContingencyPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class MultifactorAuthenticationContingencyPlan extends BaseAuthenticationRiskContingencyPlan {

    public MultifactorAuthenticationContingencyPlan(final CasConfigurationProperties casProperties,
                                                    final ApplicationContext applicationContext) {
        super(casProperties, applicationContext);
    }

    @Override
    protected AuthenticationRiskContingencyResponse executeInternal(final Authentication authentication,
                                                                    final RegisteredService service,
                                                                    final AuthenticationRiskScore score,
                                                                    final HttpServletRequest request) {
        var id = casProperties.getAuthn().getAdaptive().getRisk().getResponse().getMfaProvider();
        if (StringUtils.isBlank(id)) {
            LOGGER.debug("No explicit multifactor authentication provider is defined to handle risk-based authentication.");
            val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
            if (providerMap.isEmpty()) {
                LOGGER.warn("No multifactor authentication providers are available in the application context. Authentication is blocked");
                throw new AuthenticationException(new RiskyAuthenticationException());
            }

            if (providerMap.size() == 1) {
                id = providerMap.values().iterator().next().getId();
            } else {
                LOGGER.warn("No multifactor authentication providers are specified to handle risk-based authentication");
                throw new AuthenticationException(new MultifactorAuthenticationProviderAbsentException());
            }
        }

        LOGGER.debug("Attempting to handle risk-based authentication via multifactor authentication provider [{}]", id);
        val attributeName = casProperties.getAuthn().getAdaptive().getRisk().getResponse().getRiskyAuthenticationAttribute();
        val newAuthn = DefaultAuthenticationBuilder.newInstance(authentication)
            .addAttribute(attributeName, Boolean.TRUE)
            .build();
        LOGGER.debug("Updated authentication to remember risk-based authentication via [{}]", attributeName);
        authentication.updateAttributes(newAuthn);
        return new AuthenticationRiskContingencyResponse(new Event(this, id));
    }
}
