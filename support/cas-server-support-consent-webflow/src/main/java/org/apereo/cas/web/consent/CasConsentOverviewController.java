package org.apereo.cas.web.consent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This is {@link CasConsentOverviewController}.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
@Controller("casConsentOverviewController")
@RequestMapping("/consent")
public class CasConsentOverviewController {

    /**
     * Show consent decisions.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view where json data will be rendered
     */
    @GetMapping
    public String showConsent(final HttpServletRequest request,
                                        final HttpServletResponse response) {
        return "casConsentOverviewView";
    }
}
