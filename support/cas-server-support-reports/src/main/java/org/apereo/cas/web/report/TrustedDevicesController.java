package org.apereo.cas.web.report;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
}
