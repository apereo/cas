package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Collection;

/**
 * This is {@link IpAddressAuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class IpAddressAuthenticationRequestRiskCalculator extends BaseAuthenticationRequestRiskCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpAddressAuthenticationRequestRiskCalculator.class);
    
    public IpAddressAuthenticationRequestRiskCalculator(final CasEventRepository casEventRepository) {
        super(casEventRepository);
    }

    @Override
    protected BigDecimal calculateScore(final HttpServletRequest request,
                                        final Authentication authentication,
                                        final RegisteredService service,
                                        final Collection<CasEvent> events) {
        final String remoteAddr = ClientInfoHolder.getClientInfo().getClientIpAddress();
        LOGGER.debug("Filtering authentication events for ip address [{}]", remoteAddr);
        final long count = events.stream().filter(e -> e.getClientIpAddress().equalsIgnoreCase(remoteAddr)).count();
        LOGGER.debug("Total authentication events found for [{}]: [{}]", remoteAddr, count);
        if (count == events.size()) {
            LOGGER.debug("Principal [{}] has always authenticated from [{}]", authentication.getPrincipal(), remoteAddr);
            return LOWEST_RISK_SCORE;
        }
        return getFinalAveragedScore(count, events.size());
    }
}
