package org.apereo.cas.web.consent;

import org.apereo.cas.consent.ConsentDecision;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * This is {@link CasConsentReviewController}.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
@Controller("casConsentReviewController")
@RequestMapping("/consent")
public class CasConsentReviewController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasConsentReviewController.class);

    private final ConsentRepository consentRepository;
    private final ConsentEngine consentEngine;

    public CasConsentReviewController(final ConsentRepository consentRepository, final ConsentEngine consentEngine) {
        this.consentRepository = consentRepository;
        this.consentEngine = consentEngine;
    }

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
        return "casConsentReviewView";
    }

    /**
     * Endpoint for getting consent decisions in JSON format.
     *
     * @param request  the request
     * @param response the response
     * @return the consent decisions
     */
    @GetMapping("/getConsentDecisions")
    @ResponseBody
    public Collection<Map<String, Object>> getConsentDecisions(final HttpServletRequest request,
                                                            final HttpServletResponse response) {
        final String principal = WebUtils.getPac4jAuthenticatedUsername();
        if (!PrincipalResolver.UNKNOWN_USER.equals(principal)) {
            LOGGER.debug("Fetching consent decisions for principal [{}]", principal);
            final Collection<ConsentDecision> consentDecisions = this.consentRepository.findConsentDecisions(principal);
            LOGGER.debug("Resolved consent decisions for principal [{}]: {}", principal, consentDecisions);
            final Collection<Map<String, Object>> result = new HashSet<>();
            consentDecisions.stream().forEach(d -> {
                final Map<String, Object> map = new HashMap<>();
                map.put("decision", d);
                map.put("attributes", this.consentEngine.resolveConsentableAttributesFrom(d));
                result.add(map);
            });
            return result;
        }
        return null;
    }
}
