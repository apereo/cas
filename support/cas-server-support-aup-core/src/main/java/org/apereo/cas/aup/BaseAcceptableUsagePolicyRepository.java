package org.apereo.cas.aup;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link BaseAcceptableUsagePolicyRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseAcceptableUsagePolicyRepository implements AcceptableUsagePolicyRepository {
    private static final long serialVersionUID = 1883808902502739L;

    /**
     * Ticket registry support.
     */
    protected final transient TicketRegistrySupport ticketRegistrySupport;

    /**
     * Acceptable usage policy properties.
     */
    protected final AcceptableUsagePolicyProperties aupProperties;

    private static String getPolicyText(final RequestContext requestContext) {
        val registeredService = WebUtils.getRegisteredService(requestContext);
        if (registeredService != null && registeredService.getAcceptableUsagePolicy() != null
            && StringUtils.isNotBlank(registeredService.getAcceptableUsagePolicy().getText())) {
            return registeredService.getAcceptableUsagePolicy().getText();
        }
        return null;
    }

    @Override
    public AcceptableUsagePolicyStatus verify(final RequestContext requestContext) {
        val principal = WebUtils.getAuthentication(requestContext).getPrincipal();

        if (isUsagePolicyAcceptedBy(principal)) {
            LOGGER.debug("Usage policy has been accepted by [{}]", principal.getId());
            return AcceptableUsagePolicyStatus.accepted(principal);
        }

        LOGGER.warn("Usage policy has not been accepted by [{}]", principal.getId());
        return AcceptableUsagePolicyStatus.denied(principal);
    }

    @Override
    public Optional<AcceptableUsagePolicyTerms> fetchPolicy(final RequestContext requestContext) {
        val principal = WebUtils.getAuthentication(requestContext).getPrincipal();

        val attributes = principal.getAttributes();
        LOGGER.debug("Principal attributes found for [{}] are [{}]", principal.getId(), attributes);

        val code = getPolicyMessageBundleCode(requestContext);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val appCtx = requestContext.getActiveFlow().getApplicationContext();

        val message = appCtx.getMessage(code, null, StringUtils.EMPTY, request.getLocale());
        val terms = AcceptableUsagePolicyTerms.builder()
            .code(StringUtils.isNotBlank(message) ? code : null)
            .defaultText(getPolicyText(requestContext))
            .build();
        if (terms.isDefined()) {
            return Optional.of(terms);
        }
        return Optional.empty();
    }

    /**
     * Gets policy message bundle code.
     *
     * @param requestContext the request context
     * @return the policy message bundle code
     */
    protected String getPolicyMessageBundleCode(final RequestContext requestContext) {
        val registeredService = WebUtils.getRegisteredService(requestContext);
        if (registeredService != null && registeredService.getAcceptableUsagePolicy() != null
            && StringUtils.isNotBlank(registeredService.getAcceptableUsagePolicy().getMessageCode())) {
            return registeredService.getAcceptableUsagePolicy().getMessageCode();
        }

        if (StringUtils.isBlank(aupProperties.getCore().getAupPolicyTermsAttributeName())) {
            return null;
        }

        val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
        val attributes = principal.getAttributes();

        if (!attributes.containsKey(aupProperties.getCore().getAupPolicyTermsAttributeName())) {
            LOGGER.trace("No attribute for policy terms is defined");
            return null;
        }

        val value = CollectionUtils.firstElement(attributes.get(aupProperties.getCore().getAupPolicyTermsAttributeName()));
        return value.map(v -> String.format("%s.%s", AcceptableUsagePolicyTerms.CODE, value.get())).orElse(null);
    }

    /**
     * Is usage policy accepted by user?
     * Looks into the attributes collected by the principal to find the correct attribute, specified in settings..
     * If the attribute contains {@code true}, then the policy is determined as accepted.
     *
     * @param principal the principal
     * @return true if accepted, false otherwise.
     */
    protected boolean isUsagePolicyAcceptedBy(final Principal principal) {
        val attributes = principal.getAttributes();
        LOGGER.debug("Principal attributes found for [{}] are [{}]", principal.getId(), attributes);
        return isUsagePolicyAcceptedBy(attributes);
    }

    /**
     * Is usage policy accepted by.
     *
     * @param attributes the attributes
     * @return the boolean
     */
    protected boolean isUsagePolicyAcceptedBy(final Map<String, List<Object>> attributes) {
        val core = aupProperties.getCore();
        if (attributes != null && attributes.containsKey(core.getAupAttributeName())) {
            val value = CollectionUtils.toCollection(attributes.get(core.getAupAttributeName()));
            LOGGER.debug("Evaluating attribute value [{}] found for [{}]", value, core.getAupAttributeName());
            return value.stream().anyMatch(v -> v.toString().equalsIgnoreCase(getAcceptedAttributeValue()));
        }
        return false;
    }

    /**
     * Gets accepted attribute value.
     *
     * @return the accepted attribute value
     */
    protected String getAcceptedAttributeValue() {
        return Boolean.TRUE.toString();
    }
}
