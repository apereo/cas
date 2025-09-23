package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationVerifiedEvent;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * This is {@link RiskAuthenticationCheckTokenAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class RiskAuthenticationCheckTokenAction extends BaseCasWebflowAction {
    /**
     * Parameter name that represents the risk token.
     */
    public static final String PARAMETER_NAME_RISK_TOKEN = "rsk";

    protected final CasEventRepository casEventRepository;
    
    protected final CommunicationsManager communicationsManager;

    protected final ServicesManager servicesManager;

    protected final PrincipalResolver principalResolver;

    protected final CipherExecutor riskVerificationCipherExecutor;

    protected final ObjectProvider<GeoLocationService> geoLocationService;

    protected final CasConfigurationProperties casProperties;

    protected final ServiceFactory serviceFactory;

    protected final TenantExtractor tenantExtractor;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        try {
            val applicationContext = requestContext.getActiveFlow().getApplicationContext();
            val riskToken = requestContext.getRequestParameters().getRequired(PARAMETER_NAME_RISK_TOKEN);
            val jwtBuilder = new JwtBuilder(riskVerificationCipherExecutor, applicationContext,
                servicesManager, principalResolver, casProperties, serviceFactory);
            val jwtClaims = jwtBuilder.unpack(riskToken);

            val event = new CasEvent();
            event.setType(CasRiskyAuthenticationVerifiedEvent.class.getCanonicalName());
            val nowInEpoch = Instant.now(Clock.systemUTC()).toEpochMilli();
            event.putTimestamp(nowInEpoch);
            val dt = DateTimeUtils.zonedDateTimeOf(nowInEpoch);
            event.setCreationTime(dt.toInstant());
            event.put("riskToken", riskToken);
            event.putClientIpAddress(jwtClaims.getStringClaim("clientIpAddress"));
            event.putServerIpAddress(ClientInfoHolder.getClientInfo().getServerIpAddress());
            event.putAgent(jwtClaims.getStringClaim("userAgent"));
            val geoLocationRequest = HttpRequestUtils.getHttpServletRequestGeoLocation(jwtClaims.getStringClaim("geoLocation"));
            event.putGeoLocation(geoLocationRequest);
            event.setPrincipalId(jwtClaims.getSubject());
            tenantExtractor.extract(requestContext).ifPresent(tenant -> event.putTenant(tenant.getId()));

            val now = LocalDateTime.now(Clock.systemUTC());
            val expirationDate = DateTimeUtils.localDateTimeOf(jwtClaims.getExpirationTime());
            if (now.isBefore(expirationDate)) {
                casEventRepository.save(event);
                return success();
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }

        WebUtils.addErrorMessageToContext(requestContext, "screen.risk.authnconfirmed.message");
        return error();
    }
}
