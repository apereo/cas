package org.apereo.cas.impl.notify;

import module java.base;
import org.apereo.cas.api.AuthenticationRiskNotifier;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.RiskAuthenticationCheckTokenAction;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.apache.hc.core5.net.URIBuilder;
import org.apereo.inspektr.common.web.ClientInfo;
import org.springframework.context.ApplicationContext;

/**
 * This is {@link BaseAuthenticationRiskNotifier}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Setter
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseAuthenticationRiskNotifier implements AuthenticationRiskNotifier {

    protected final ApplicationContext applicationContext;
    
    protected final CasConfigurationProperties casProperties;

    protected final CommunicationsManager communicationsManager;

    protected final ServicesManager servicesManager;
    
    protected final PrincipalResolver principalResolver;

    protected final CipherExecutor riskVerificationCipherExecutor;

    protected final ServiceFactory serviceFactory;

    protected final TenantExtractor tenantExtractor;

    protected Authentication authentication;

    protected RegisteredService registeredService;

    protected AuthenticationRiskScore authenticationRiskScore;

    protected ClientInfo clientInfo;

    @Override
    public void run() {
        FunctionUtils.doUnchecked(_ -> publish());
    }

    protected String buildRiskVerificationUrl() {
        return FunctionUtils.doUnchecked(() -> {
            val riskToken = createRiskToken();
            return new URIBuilder(casProperties.getServer().getPrefix())
                .appendPath(CasWebflowConfigurer.FLOW_ID_RISK_VERIFICATION)
                .addParameter(RiskAuthenticationCheckTokenAction.PARAMETER_NAME_RISK_TOKEN, riskToken)
                .build()
                .toString();
        });
    }

    @Override
    public String createRiskToken() throws Throwable {
        val jwtBuilder = new JwtBuilder(riskVerificationCipherExecutor,
            applicationContext, servicesManager, principalResolver, casProperties, serviceFactory);
        val expiration = Beans.newDuration(casProperties.getAuthn().getAdaptive()
            .getRisk().getResponse().getRiskVerificationTokenExpiration());
        val expirationDate = DateTimeUtils.dateOf(LocalDateTime.now(Clock.systemUTC()).plus(expiration));
        val attributes = new HashMap<String, List<Object>>();
        attributes.put("clientIpAddress", List.of(clientInfo.getClientIpAddress()));
        attributes.put("userAgent", List.of(clientInfo.getUserAgent()));
        attributes.put("geoLocation", List.of(clientInfo.getGeoLocation()));
        val jwtRequest = JwtBuilder.JwtRequest
            .builder()
            .serviceAudience(Set.of(casProperties.getServer().getPrefix()))
            .subject(authentication.getPrincipal().getId())
            .jwtId(UUID.randomUUID().toString())
            .registeredService(Optional.of(registeredService))
            .issuer(casProperties.getServer().getName())
            .validUntilDate(expirationDate)
            .attributes(attributes)
            .build();
        return jwtBuilder.build(jwtRequest);
    }
}
