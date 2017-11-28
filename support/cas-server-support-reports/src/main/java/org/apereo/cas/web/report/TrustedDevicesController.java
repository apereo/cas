package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.web.BaseCasMvcEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.Set;

/**
 * This is {@link TrustedDevicesController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConditionalOnClass(value = MultifactorAuthenticationTrustStorage.class)
public class TrustedDevicesController extends BaseCasMvcEndpoint {

    private final MultifactorAuthenticationTrustStorage mfaTrustEngine;
    private final CasConfigurationProperties casProperties;

    public TrustedDevicesController(final MultifactorAuthenticationTrustStorage mfaTrustEngine,
                                    final CasConfigurationProperties casProperties) {
        super("trustedDevs", "/trustedDevs", casProperties.getMonitor().getEndpoints().getTrustedDevices(), casProperties);
        this.mfaTrustEngine = mfaTrustEngine;
        this.casProperties = casProperties;
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @GetMapping
    protected ModelAndView handleRequestInternal(final HttpServletRequest request,
                                                 final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);

        return new ModelAndView("monitoring/viewTrustedDevices");
    }

    /**
     * Gets records.
     *
     * @param request  the request
     * @param response the response
     * @return the records
     */
    @GetMapping(value = "/getRecords")
    @ResponseBody
    public Set<MultifactorAuthenticationTrustRecord> getRecords(final HttpServletRequest request,
                                                                final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);

        final TrustedDevicesMultifactorProperties trusted = casProperties.getAuthn().getMfa().getTrusted();
        final LocalDate onOrAfter = LocalDate.now().minus(trusted.getExpiration(), DateTimeUtils.toChronoUnit(trusted.getTimeUnit()));

        this.mfaTrustEngine.expire(onOrAfter);
        return this.mfaTrustEngine.get(onOrAfter);
    }

    /**
     * Revoke record.
     *
     * @param key      the key
     * @param request  the request
     * @param response the response
     * @return the integer
     */
    @PostMapping(value = "/revokeRecord")
    @ResponseBody
    public Integer revokeRecord(@RequestParam final String key, final HttpServletRequest request,
                                final HttpServletResponse response) {
        ensureEndpointAccessIsAuthorized(request, response);

        this.mfaTrustEngine.expire(key);
        return HttpStatus.OK.value();
    }
}
