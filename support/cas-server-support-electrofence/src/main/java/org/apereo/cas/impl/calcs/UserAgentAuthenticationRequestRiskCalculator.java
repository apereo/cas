package org.apereo.cas.impl.calcs;

import org.apereo.cas.support.events.dao.CasEventRepository;

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
}
