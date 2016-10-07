package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.dao.CasEventRepository;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * This is {@link IpAddressAuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class IpAddressAuthenticationRequestRiskCalculator extends BaseAuthenticationRequestRiskCalculator {

    public IpAddressAuthenticationRequestRiskCalculator(final CasEventRepository casEventRepository) {
        super(casEventRepository);
    }

    @Override
    protected double calculateScore(final HttpServletRequest request,
                                    final Authentication authentication,
                                    final RegisteredService service,
                                    final Collection<CasEvent> events) {
        final String remoteAddr = ClientInfoHolder.getClientInfo().getClientIpAddress();
        logger.debug("Filtering authentication events for ip address {}", remoteAddr);
        final long count = events.stream().filter(e -> e.getClientIpAddress().equalsIgnoreCase(remoteAddr)).count();
        logger.debug("Total authentication events found for {}: {}", remoteAddr, count);
        
        if (events.isEmpty()) {
            return HIGHEST_RISK_SCORE;
        }
        
        if (count == events.size()) {
            logger.debug("Principal {} has always authenticated from {}", authentication.getPrincipal(), remoteAddr);
            return LOWEST_RISK_SCORE;
        }
        return HIGHEST_RISK_SCORE - (count / events.size());
    }
}
