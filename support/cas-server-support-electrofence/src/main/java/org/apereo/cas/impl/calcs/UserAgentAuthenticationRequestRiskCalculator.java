package org.apereo.cas.impl.calcs;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Collection;

/**
 * This is {@link UserAgentAuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class UserAgentAuthenticationRequestRiskCalculator extends BaseAuthenticationRequestRiskCalculator {

    public UserAgentAuthenticationRequestRiskCalculator(final CasEventRepository casEventRepository,
                                                        final CasConfigurationProperties casProperties) {
        super(casEventRepository, casProperties);
    }

    @Override
    protected BigDecimal calculateScore(final HttpServletRequest request,
                                        final Authentication authentication,
                                        final RegisteredService service,
                                        final Collection<? extends CasEvent> events) {

        val agent = HttpRequestUtils.getHttpServletRequestUserAgent(request);
        LOGGER.debug("Filtering authentication events for user agent [{}]", agent);
        val count = events.stream()
            .filter(e -> StringUtils.isNotBlank(e.getAgent()))
            .filter(e -> e.getAgent().equalsIgnoreCase(agent))
            .count();
        LOGGER.debug("Total authentication events found for [{}]: [{}]", agent, count);
        return calculateScoreBasedOnEventsCount(authentication, events, count);
    }
}
