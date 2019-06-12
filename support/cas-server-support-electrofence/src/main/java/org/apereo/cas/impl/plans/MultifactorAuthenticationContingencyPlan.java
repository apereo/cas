package org.apereo.cas.impl.plans;

import org.apereo.cas.api.AuthenticationRiskContingencyResponse;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.execution.Event;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link MultifactorAuthenticationContingencyPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class MultifactorAuthenticationContingencyPlan extends BaseAuthenticationRiskContingencyPlan {

    public MultifactorAuthenticationContingencyPlan(final CasConfigurationProperties casProperties, final ApplicationContext applicationContext) {
        super(casProperties, applicationContext);
    }

    @Override
    protected AuthenticationRiskContingencyResponse executeInternal(final Authentication authentication,
                                                                    final RegisteredService service,
                                                                    final AuthenticationRiskScore score,
                                                                    final HttpServletRequest request) {
        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap.isEmpty()) {
            LOGGER.warn("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }

        var id = casProperties.getAuthn().getAdaptive().getRisk().getResponse().getMfaProvider();
        if (StringUtils.isBlank(id)) {
            if (providerMap.size() == 1) {
                id = providerMap.values().iterator().next().getId();
            } else {
                LOGGER.warn("No multifactor authentication providers are specified to handle risk-based authentication");
                throw new AuthenticationException();
            }
        }

        val attributeName = casProperties.getAuthn().getAdaptive().getRisk().getResponse().getRiskyAuthenticationAttribute();
        val newAuthn = DefaultAuthenticationBuilder.newInstance(authentication)
            .addAttribute(attributeName, Boolean.TRUE)
            .build();
        LOGGER.debug("Updated authentication to remember risk-based authn via [{}]", attributeName);
        authentication.update(newAuthn);
        return new AuthenticationRiskContingencyResponse(new Event(this, id));
    }
}
