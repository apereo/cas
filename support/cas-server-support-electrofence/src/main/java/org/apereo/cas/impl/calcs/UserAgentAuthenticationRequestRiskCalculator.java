package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.dao.CasEventRepository;
import org.apereo.cas.web.support.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * This is {@link UserAgentAuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class UserAgentAuthenticationRequestRiskCalculator extends BaseAuthenticationRequestRiskCalculator {
    public UserAgentAuthenticationRequestRiskCalculator(final CasEventRepository casEventRepository) {
        super(casEventRepository);
    }

    @Override
    protected double calculateScore(final HttpServletRequest request,
                                    final Authentication authentication,
                                    final RegisteredService service,
                                    final Collection<CasEvent> events) {
        
        final String agent = WebUtils.getHttpServletRequestUserAgent(request);
        logger.debug("Filtering authentication events for user agent {}", agent);
        final long count = events.stream().filter(e -> e.getAgent().equalsIgnoreCase(agent)).count();
        logger.debug("Total authentication events found for {}: {}", agent, count);
        if (count == events.size()) {
            logger.debug("Principal {} has always authenticated from {}", authentication.getPrincipal(), agent);
            return LOWEST_RISK_SCORE;
        }
        return HIGHEST_RISK_SCORE - (count / events.size());
    }
}
