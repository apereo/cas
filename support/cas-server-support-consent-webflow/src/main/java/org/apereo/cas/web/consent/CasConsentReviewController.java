package org.apereo.cas.web.consent;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.util.Pac4jUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.pac4j.core.config.Config;
import org.pac4j.core.http.adapter.J2ENopHttpActionAdapter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
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
@RequestMapping("/consentReview")
@Slf4j
@RequiredArgsConstructor
public class CasConsentReviewController {
    private static final String CONSENT_REVIEW_VIEW = "casConsentReviewView";

    private final ConsentRepository consentRepository;
    private final ConsentEngine consentEngine;
    private final Config pac4jConfig;
    private final CasConfigurationProperties casProperties;

    /**
     * Show consent decisions.
     *
     * @param request  the request
     * @param response the response
     * @return the view where json data will be rendered
     */
    @GetMapping
    public ModelAndView showConsent(final HttpServletRequest request,
                                    final HttpServletResponse response) {
        val view = new ModelAndView(CONSENT_REVIEW_VIEW);
        view.getModel().put("principal", Pac4jUtils.getPac4jAuthenticatedUsername());
        return view;
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
        val principal = Pac4jUtils.getPac4jAuthenticatedUsername();
        if (!PrincipalResolver.UNKNOWN_USER.equals(principal)) {
            LOGGER.debug("Fetching consent decisions for principal [{}]", principal);
            val consentDecisions = this.consentRepository.findConsentDecisions(principal);
            LOGGER.debug("Resolved consent decisions for principal [{}]: {}", principal, consentDecisions);
            val result = new HashSet<Map<String, Object>>();
            consentDecisions.forEach(d -> {
                val map = new HashMap<String, Object>();
                map.put("decision", d);
                map.put("attributes", this.consentEngine.resolveConsentableAttributesFrom(d));
                result.add(map);
            });
            return result;
        }
        return new ArrayList<>();
    }

    /**
     * Endpoint for deleting single consent decisions.
     *
     * @param decisionId the decision id
     * @return true / false
     */
    @PostMapping("/deleteConsentDecision")
    @ResponseBody
    public boolean deleteConsentDecision(@RequestParam final Long decisionId) {
        val principal = Pac4jUtils.getPac4jAuthenticatedUsername();
        LOGGER.debug("Deleting consent decision with id [{}] for principal [{}].", decisionId, principal);
        return this.consentRepository.deleteConsentDecision(decisionId, principal);
    }

    /**
     * Endpoint for Cas Client Callback.
     *
     * @param request  the request
     * @param response the response
     */
    @GetMapping("/callback")
    public void consentCallback(final HttpServletRequest request, final HttpServletResponse response) {
        LOGGER.debug("Callback endpoint hit...");

        val logic = this.pac4jConfig.getCallbackLogic();
        val context = Pac4jUtils.getPac4jJ2EContext(request, response);
        val defaultUrl = this.casProperties.getServer().getPrefix().concat("/consentReview");
        logic.perform(context, this.pac4jConfig, J2ENopHttpActionAdapter.INSTANCE,
            defaultUrl, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null);
    }
}
