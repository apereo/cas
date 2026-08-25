package org.apereo.cas.aup;

import module java.base;
import org.apereo.cas.services.WebBasedRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AcceptableUsagePolicyRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public interface AcceptableUsagePolicyRepository extends Serializable {
    /**
     * Logger instance.
     */
    Logger LOGGER = LoggerFactory.getLogger(AcceptableUsagePolicyRepository.class);

    /**
     * Condition to activate AUP.
     */
    BeanCondition CONDITION_AUP_ENABLED = BeanCondition.on("cas.acceptable-usage-policy.core.enabled").isTrue().evenIfMissing();

    /**
     * Default bean name.
     */
    String BEAN_NAME = "acceptableUsagePolicyRepository";

    /**
     * No op acceptable usage policy repository.
     *
     * @return the acceptable usage policy repository
     */
    static AcceptableUsagePolicyRepository noOp() {
        return new AcceptableUsagePolicyRepository() {
            @Serial
            private static final long serialVersionUID = 8784500942988440997L;

            @Override
            public AcceptableUsagePolicyStatus verify(final RequestContext requestContext) {
                val authn = WebUtils.getAuthentication(requestContext);
                return AcceptableUsagePolicyStatus.skipped(authn.getPrincipal());
            }

            @Override
            public boolean submit(final RequestContext requestContext) {
                return false;
            }

            @Override
            public Optional<AcceptableUsagePolicyTerms> fetchPolicy(final RequestContext requestContext) {
                return Optional.empty();
            }
        };
    }

    /**
     * Verify whether the policy is accepted.
     *
     * @param requestContext the request context
     * @return result /status if policy is accepted along with principal.
     * @throws Throwable the throwable
     */
    AcceptableUsagePolicyStatus verify(RequestContext requestContext) throws Throwable;

    /**
     * Record the fact that the policy is accepted..
     *
     * @param requestContext the request context
     * @return true if choice was saved.
     * @throws Throwable the throwable
     */
    boolean submit(RequestContext requestContext) throws Throwable;

    /**
     * Fetch policy as optional.
     *
     * @param requestContext the request context
     * @return the optional
     */
    default Optional<AcceptableUsagePolicyTerms> fetchPolicy(final RequestContext requestContext) {
        val principal = WebUtils.getAuthentication(requestContext).getPrincipal();

        val attributes = principal.getAttributes();
        LOGGER.debug("Principal attributes found for [{}] are [{}]", principal.getId(), attributes);

        val code = StringUtils.defaultString(getPolicyMessageBundleCode(requestContext));
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val applicationContext = requestContext.getActiveFlow().getApplicationContext();

        val message = applicationContext.getMessage(code, null, StringUtils.EMPTY, request.getLocale());
        val terms = AcceptableUsagePolicyTerms.builder()
            .code(StringUtils.isNotBlank(message) ? code : null)
            .defaultText(getPolicyText(requestContext))
            .build();
        return terms.isDefined() ? Optional.of(terms) : Optional.empty();
    }


    private static @Nullable String getPolicyText(final RequestContext requestContext) {
        val registeredService = (WebBasedRegisteredService) WebUtils.getRegisteredService(requestContext);
        if (registeredService != null && registeredService.getAcceptableUsagePolicy() != null
            && StringUtils.isNotBlank(registeredService.getAcceptableUsagePolicy().getText())) {
            return registeredService.getAcceptableUsagePolicy().getText();
        }
        return null;
    }

    private @Nullable String getPolicyMessageBundleCode(final RequestContext requestContext) {
        val registeredService = (WebBasedRegisteredService) WebUtils.getRegisteredService(requestContext);
        if (registeredService != null && registeredService.getAcceptableUsagePolicy() != null
            && StringUtils.isNotBlank(registeredService.getAcceptableUsagePolicy().getMessageCode())) {
            return registeredService.getAcceptableUsagePolicy().getMessageCode();
        }

        val aupProperties = ApplicationContextProvider.getCasConfigurationProperties()
            .orElseThrow()
            .getAcceptableUsagePolicy()
            .getCore();
        if (StringUtils.isBlank(aupProperties.getAupPolicyTermsAttributeName())) {
            return null;
        }

        val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
        val attributes = principal.getAttributes();

        if (!attributes.containsKey(aupProperties.getAupPolicyTermsAttributeName())) {
            LOGGER.trace("No attribute for policy terms is defined");
            return null;
        }

        val value = CollectionUtils.firstElement(attributes.get(aupProperties.getAupPolicyTermsAttributeName()));
        return value.map(v -> String.format("%s.%s", AcceptableUsagePolicyTerms.CODE, value.get())).orElse(null);
    }
}
