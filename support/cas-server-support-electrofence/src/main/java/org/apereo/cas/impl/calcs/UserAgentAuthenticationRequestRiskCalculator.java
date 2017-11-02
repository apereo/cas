package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.util.HttpRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Collection;

/**
 * This is {@link UserAgentAuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class UserAgentAuthenticationRequestRiskCalculator extends BaseAuthenticationRequestRiskCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserAgentAuthenticationRequestRiskCalculator.class);
    
    public UserAgentAuthenticationRequestRiskCalculator(final CasEventRepository casEventRepository) {
        super(casEventRepository);
    }

    @Override
    protected BigDecimal calculateScore(final HttpServletRequest request,
                                        final Authentication authentication,
                                        final RegisteredService service,
                                        final Collection<CasEvent> events) {

        final String agent = HttpRequestUtils.getHttpServletRequestUserAgent(request);
        LOGGER.debug("Filtering authentication events for user agent [{}]", agent);
        final long count = events.stream().filter(e -> e.getAgent().equalsIgnoreCase(agent)).count();
        LOGGER.debug("Total authentication events found for [{}]: [{}]", agent, count);
        if (count == events.size()) {
            LOGGER.debug("Principal [{}] has always authenticated from [{}]", authentication.getPrincipal(), agent);
            return LOWEST_RISK_SCORE;
        }
        return getFinalAveragedScore(count, events.size());
    }
}
