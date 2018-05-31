package org.apereo.cas.web.consent;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.pac4j.core.config.Config;
import org.pac4j.core.http.adapter.J2ENopHttpActionAdapter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.WebAsyncTask;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * This is {@link CasConsentReviewController}.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
@Controller("casConsentReviewController")
@RequestMapping("/consentReview")
@Slf4j
@AllArgsConstructor
public class CasConsentReviewController {

    private static final String CONSENT_REVIEW_VIEW = "casConsentReviewView";
    private static final String CONSENT_LOGOUT_VIEW = "casConsentLogoutView";
    
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
        final var view = new ModelAndView(CONSENT_REVIEW_VIEW);
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
    public WebAsyncTask<Collection<Map<String, Object>>> getConsentDecisions(final HttpServletRequest request,
                                                            final HttpServletResponse response) {
        final Callable<Collection<Map<String, Object>>> asyncTask = () -> {
            final var principal = Pac4jUtils.getPac4jAuthenticatedUsername();
            if (!PrincipalResolver.UNKNOWN_USER.equals(principal)) {
                LOGGER.debug("Fetching consent decisions for principal [{}]", principal);
                final var consentDecisions = this.consentRepository.findConsentDecisions(principal);
                LOGGER.debug("Resolved consent decisions for principal [{}]: {}", principal, consentDecisions);
                final Collection<Map<String, Object>> result = new HashSet<>();
                consentDecisions.forEach(d -> {
                    final Map<String, Object> map = new HashMap<>();
                    map.put("decision", d);
                    map.put("attributes", this.consentEngine.resolveConsentableAttributesFrom(d));
                    result.add(map);
                });
                return result;
            }
            return null;
        };
        final var timeout = Beans.newDuration(casProperties.getHttpClient().getAsyncTimeout()).toMillis();
        return new WebAsyncTask<>(timeout, asyncTask);
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
        final var principal = Pac4jUtils.getPac4jAuthenticatedUsername();
        LOGGER.debug("Deleting consent decision with id [{}] for principal [{}].", decisionId, principal);        
        return this.consentRepository.deleteConsentDecision(decisionId, principal);
    }
    
    /**
     * Endpoint for local logout, no SLO.
     * 
     * @param request the request
     * @param response the response
     * @return the logout view
     */
    @GetMapping("/logout")
    public String logout(final HttpServletRequest request, final HttpServletResponse response) {
        LOGGER.debug("Performing Pac4j logout...");
        final var manager = Pac4jUtils.getPac4jProfileManager(request, response);
        manager.logout();
        return CONSENT_LOGOUT_VIEW;
    }
    
    /**
     * Endpoint for Cas Client Callback.
     * 
     * @param request the request
     * @param response the response
     */
    @GetMapping("/callback")
    public void callback(final HttpServletRequest request, final HttpServletResponse response) {
        LOGGER.debug("Callback endpoint hit...");
        
        final var logic = this.pac4jConfig.getCallbackLogic();
        final var context = Pac4jUtils.getPac4jJ2EContext(request, response);
        final var defaultUrl = this.casProperties.getServer().getPrefix().concat("/consentReview");
        logic.perform(context, this.pac4jConfig, J2ENopHttpActionAdapter.INSTANCE,
            defaultUrl, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null);
    }
}
