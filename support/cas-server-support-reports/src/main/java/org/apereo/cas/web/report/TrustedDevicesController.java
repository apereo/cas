package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
@Controller("trustedDevicesController")
@RequestMapping("/status/trustedDevs")
@ConditionalOnClass(value = MultifactorAuthenticationTrustStorage.class)
public class TrustedDevicesController {

    @Autowired
    private CasConfigurationProperties casProperties;

    private final MultifactorAuthenticationTrustStorage mfaTrustEngine;

    public TrustedDevicesController(final MultifactorAuthenticationTrustStorage mfaTrustEngine) {
        this.mfaTrustEngine = mfaTrustEngine;
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @RequestMapping(method = RequestMethod.GET)
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        return new ModelAndView("monitoring/viewTrustedDevices");
    }

    /**
     * Gets records.
     *
     * @return the records
     * @throws Exception the exception
     */
    @RequestMapping(value = "/getRecords", method = RequestMethod.GET)
    @ResponseBody
    public Set<MultifactorAuthenticationTrustRecord> getRecords() throws Exception {
        final LocalDate onOrAfter = LocalDate.now().minus(casProperties.getAuthn().getMfa().getTrusted().getExpiration(),
                DateTimeUtils.toChronoUnit(casProperties.getAuthn().getMfa().getTrusted().getTimeUnit()));
        this.mfaTrustEngine.expire(onOrAfter);
        return this.mfaTrustEngine.get(onOrAfter);
    }

    /**
     * Revoke record.
     *
     * @param key     the key
     * @param request the request
     * @return the integer
     * @throws Exception the exception
     */
    @RequestMapping(value = "/revokeRecord", method = RequestMethod.POST)
    @ResponseBody
    public Integer revokeRecord(@RequestParam final String key,
                                final HttpServletRequest request) throws Exception {
        this.mfaTrustEngine.expire(key);
        return HttpStatus.OK.value();
    }
}
