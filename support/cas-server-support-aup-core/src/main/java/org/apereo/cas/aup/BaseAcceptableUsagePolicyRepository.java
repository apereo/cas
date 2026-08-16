package org.apereo.cas.aup;

import module java.base;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link BaseAcceptableUsagePolicyRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseAcceptableUsagePolicyRepository implements AcceptableUsagePolicyRepository {
    @Serial
    private static final long serialVersionUID = 1883808902502739L;

    /**
     * Acceptable usage policy properties.
     */
    protected final AcceptableUsagePolicyProperties aupProperties;
    
    @Override
    public AcceptableUsagePolicyStatus verify(final RequestContext requestContext) throws Throwable {
        val authentication = WebUtils.getAuthentication(requestContext);
        if (authentication == null) {
            throw new AuthenticationException("Unable to determine authentication from the request context");
        }
        val principal = authentication.getPrincipal();

        if (isUsagePolicyAcceptedBy(principal)) {
            LOGGER.debug("Acceptable usage policy has been accepted by [{}]", principal.getId());
            return AcceptableUsagePolicyStatus.accepted(principal);
        }

        LOGGER.info("Acceptable usage policy has not been accepted by [{}]", principal.getId());
        return AcceptableUsagePolicyStatus.denied(principal);
    }

    /**
     * Determines whether the user has accepted the usage policy.
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

    protected boolean isUsagePolicyAcceptedBy(final Map<String, List<Object>> attributes) {
        val core = aupProperties.getCore();

        if (attributes != null && attributes.containsKey(core.getAupAttributeName())) {
            val value = CollectionUtils.toCollection(attributes.get(core.getAupAttributeName()));
            LOGGER.debug("Evaluating attribute value [{}] found for [{}]", value, core.getAupAttributeName());
            return value.stream().anyMatch(v -> v.toString().equalsIgnoreCase(getAcceptedAttributeValue()));
        }

        if (core.isAupOmitIfAttributeMissing()) {
            LOGGER.trace("Value for [{}] is missing.", core.getAupAttributeName());
            return true;
        }

        return false;
    }

    protected String getAcceptedAttributeValue() {
        return Boolean.TRUE.toString();
    }
}
