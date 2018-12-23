package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Collection;

/**
 * This is {@link IpAddressAuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class IpAddressAuthenticationRequestRiskCalculator extends BaseAuthenticationRequestRiskCalculator {


    public IpAddressAuthenticationRequestRiskCalculator(final CasEventRepository casEventRepository,
                                                        final CasConfigurationProperties casProperties) {
        super(casEventRepository, casProperties);
    }

    @Override
    protected BigDecimal calculateScore(final HttpServletRequest request,
                                        final Authentication authentication,
                                        final RegisteredService service,
                                        final Collection<? extends CasEvent> events) {
        val remoteAddr = ClientInfoHolder.getClientInfo().getClientIpAddress();
        LOGGER.debug("Filtering authentication events for ip address [{}]", remoteAddr);
        val count = events.stream().filter(e -> e.getClientIpAddress().equalsIgnoreCase(remoteAddr)).count();
        LOGGER.debug("Total authentication events found for [{}]: [{}]", remoteAddr, count);
        return calculateScoreBasedOnEventsCount(authentication, events, count);
    }
}
