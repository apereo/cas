package org.apereo.cas.impl.calcs;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;

/**
 * This is {@link DeviceFingerprintAuthenticationRequestRiskCalculator}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
public class DeviceFingerprintAuthenticationRequestRiskCalculator extends BaseAuthenticationRequestRiskCalculator {

    public DeviceFingerprintAuthenticationRequestRiskCalculator(final CasEventRepository casEventRepository,
                                                                final CasConfigurationProperties casProperties) {
        super(casEventRepository, casProperties);
    }

    @Override
    protected BigDecimal calculateScore(final ClientInfo clientInfo,
                                        final Authentication authentication,
                                        final RegisteredService service,
                                        final List<? extends CasEvent> events) {
        val deviceFingerprint = clientInfo.getDeviceFingerprint();
        LOGGER.debug("Filtering authentication events for device fingerprint [{}]", deviceFingerprint);
        val count = events.stream()
            .filter(e -> StringUtils.isNotBlank(e.getDeviceFingerprint()))
            .filter(e -> e.getDeviceFingerprint().equalsIgnoreCase(deviceFingerprint))
            .count();
        LOGGER.debug("Total authentication events found for [{}]: [{}]", deviceFingerprint, count);
        return calculateScoreBasedOnEventsCount(authentication, events, count);
    }
}
