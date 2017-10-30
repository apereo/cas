package org.apereo.cas.trusted.web;

import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.DateTimeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.Set;

/**
 * This is {@link MultifactorAuthenticationTrustController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RestController("mfaTrustController")
@RequestMapping(value="/status/trustedDevices")
public class MultifactorAuthenticationTrustController {

    private final MultifactorAuthenticationTrustStorage storage;
    private final TrustedDevicesMultifactorProperties trustedProperties;

    public MultifactorAuthenticationTrustController(final MultifactorAuthenticationTrustStorage storage,
                                                    final TrustedDevicesMultifactorProperties trustedProperties) {
        this.storage = storage;
        this.trustedProperties = trustedProperties;
    }

    /**
     * Gets all trusted devices.
     *
     * @param response the response
     * @param request  the request
     * @return the all trusted devices
     */
    @GetMapping
    @ResponseBody
    public Set<MultifactorAuthenticationTrustRecord> getAllTrustedDevices(final HttpServletResponse response,
                                                                          final HttpServletRequest request) {

        final LocalDate onOrAfter = LocalDate.now().minus(trustedProperties.getExpiration(),
                DateTimeUtils.toChronoUnit(trustedProperties.getTimeUnit()));
        return storage.get(onOrAfter);
    }
}
