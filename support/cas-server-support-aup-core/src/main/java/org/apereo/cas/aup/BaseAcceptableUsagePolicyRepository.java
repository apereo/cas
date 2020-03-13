package org.apereo.cas.aup;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link BaseAcceptableUsagePolicyRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
@RequiredArgsConstructor
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

    @Override
    public AcceptableUsagePolicyStatus verify(final RequestContext requestContext, final Credential credential) {
        val principal = WebUtils.getAuthentication(requestContext).getPrincipal();

        if (isUsagePolicyAcceptedBy(principal)) {
            LOGGER.debug("Usage policy has been accepted by [{}]", principal.getId());
            return AcceptableUsagePolicyStatus.accepted(principal);
        }

        LOGGER.warn("Usage policy has not been accepted by [{}]", principal.getId());
        return AcceptableUsagePolicyStatus.denied(principal);
    }

    @Override
    public Optional<AcceptableUsagePolicyTerms> fetchPolicy(final RequestContext requestContext, final Credential credential) {
        val principal = WebUtils.getAuthentication(requestContext).getPrincipal();

        val attributes = principal.getAttributes();
        LOGGER.debug("Principal attributes found for [{}] are [{}]", principal.getId(), attributes);

        if (StringUtils.isNotBlank(aupProperties.getAupPolicyTermsAttributeName())) {
            if (attributes != null && attributes.containsKey(aupProperties.getAupPolicyTermsAttributeName())) {
                val value = CollectionUtils.firstElement(attributes.get(aupProperties.getAupPolicyTermsAttributeName()));
                if (value.isPresent()) {
                    val code = String.format("%s.%s", AcceptableUsagePolicyTerms.CODE, value.get());
                    val appCtx = requestContext.getActiveFlow().getApplicationContext();
                    val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
                    val message = appCtx.getMessage(code, null, StringUtils.EMPTY, request.getLocale());
                    if (StringUtils.isNotBlank(message)) {
                        val terms = AcceptableUsagePolicyTerms.builder()
                            .code(code)
                            .build();
                        return Optional.of(terms);
                    }
                }
            }
        }
        return Optional.empty();
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

        if (attributes != null && attributes.containsKey(aupProperties.getAupAttributeName())) {
            val value = CollectionUtils.toCollection(attributes.get(aupProperties.getAupAttributeName()));
            LOGGER.debug("Evaluating attribute value [{}] found for [{}]", value, aupProperties.getAupAttributeName());
            return value.stream().anyMatch(v -> v.toString().equalsIgnoreCase(Boolean.TRUE.toString()));
        }
        return false;
    }
}
