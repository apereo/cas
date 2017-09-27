package org.apereo.cas.web.consent;

import org.apereo.cas.consent.ConsentDecision;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentRepository;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
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
import java.util.Optional;

/**
 * This is {@link CasConsentOverviewController}.
 *
 * @author Arnold Bergner
 * @since 5.2.0
 */
@Controller("casConsentOverviewController")
@RequestMapping("/consent")
public class CasConsentOverviewController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasConsentOverviewController.class);

    private final ConsentRepository consentRepository;
    private final ConsentEngine consentEngine;

    public CasConsentOverviewController(final ConsentRepository consentRepository, final ConsentEngine consentEngine) {
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
        return "casConsentOverviewView";
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
        final WebContext context = new J2EContext(request, response);
        final ProfileManager manager = new ProfileManager(context);
        final Optional<UserProfile> profile = manager.get(true);
        if (profile.isPresent()) {
            final String principal = profile.get().getId();
            LOGGER.debug("Fetching consent decisions for principal [{}]", principal);
            final Collection<ConsentDecision> consentDecisions = this.consentRepository.findConsentDecisions(principal);
            LOGGER.debug("Resolved consent decisions for principal [{}]: {}", principal, consentDecisions);
            final Collection<Map<String, Object>> result = new HashSet<>();
            consentDecisions.stream().map(this::decodeDecision).forEach(result::add);
            return result;
        }
        return null;
    }

    /**
     * Returns decoded consent decision.
     * @param decision
     * @return the decoded decision
     */
    private Map<String, Object> decodeDecision(final ConsentDecision decision) {
        final Map<String, Object> map = new HashMap<>();
        map.put("id", decision.getId());
        map.put("principal", decision.getPrincipal());
        map.put("service", decision.getService());
        map.put("createdDate", decision.getCreatedDate());
        map.put("options", decision.getOptions());
        map.put("reminder", decision.getReminder());
        map.put("reminderTimeUnit", decision.getReminderTimeUnit());
        map.put("attributes", this.consentEngine.resolveConsentableAttributesFrom(decision));
        return map;
    }
}
