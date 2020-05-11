package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link BaseMultifactorAuthenticationProviderBypassEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
@ToString
public abstract class BaseMultifactorAuthenticationProviderBypassEvaluator implements MultifactorAuthenticationProviderBypassEvaluator {
    private static final long serialVersionUID = 2372899636154131393L;
    private final String providerId;
    private final String id = this.getClass().getSimpleName();

    @Override
    public void forgetBypass(final Authentication authentication) {
        authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, Boolean.FALSE);
    }

    @Override
    public boolean isMultifactorAuthenticationBypassed(final Authentication authentication, final String requestedContext) {
        val attributes = authentication.getAttributes();
        if (attributes.containsKey(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA)) {

            val result = CollectionUtils.firstElement(attributes.get(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA));
            val providerRes = CollectionUtils.firstElement(attributes.get(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER));

            if (result.isPresent()) {
                val bypass = (Boolean) result.get();
                if (bypass && providerRes.isPresent()) {
                    val provider = providerRes.get().toString();
                    return StringUtils.equalsIgnoreCase(requestedContext, provider);
                }
            }
        }
        return false;
    }

    @Override
    public void rememberBypass(final Authentication authentication,
                               final MultifactorAuthenticationProvider provider) {
        authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, Boolean.TRUE);
        authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER, provider.getId());
    }

    @Override
    public Optional<MultifactorAuthenticationProviderBypassEvaluator> belongsToMultifactorAuthenticationProvider(final String providerId) {
        if (getProviderId().equalsIgnoreCase(providerId)) {
            return Optional.of(this);
        }
        return Optional.empty();
    }

    @Audit(action = "MFA_BYPASS",
        actionResolverName = "MFA_BYPASS_ACTION_RESOLVER",
        resourceResolverName = "MFA_BYPASS_RESOURCE_RESOLVER")
    @Override
    public boolean shouldMultifactorAuthenticationProviderExecute(final Authentication authentication, final RegisteredService registeredService,
                                                                  final MultifactorAuthenticationProvider provider, final HttpServletRequest request) {
        return shouldMultifactorAuthenticationProviderExecuteInternal(authentication, registeredService, provider, request);
    }

    /**
     * Should multifactor authentication provider execute internal.
     *
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @param provider          the provider
     * @param request           the request
     * @return true/false
     */
    protected abstract boolean shouldMultifactorAuthenticationProviderExecuteInternal(Authentication authentication,
                                                                                      RegisteredService registeredService,
                                                                                      MultifactorAuthenticationProvider provider,
                                                                                      HttpServletRequest request);


    /**
     * Locate matching attribute value boolean.
     *
     * @param attrName   the attr name
     * @param attrValue  the attr value
     * @param attributes the attributes
     * @return true/false
     */
    protected static boolean locateMatchingAttributeValue(final String attrName, final String attrValue,
                                                          final Map<String, List<Object>> attributes) {
        return locateMatchingAttributeValue(attrName, attrValue, attributes, true);
    }

    /**
     * Evaluate attribute rules for bypass.
     *
     * @param attrName               the attr name
     * @param attrValue              the attr value
     * @param attributes             the attributes
     * @param matchIfNoValueProvided the force match on value
     * @return true a matching attribute name/value is found
     */
    protected static boolean locateMatchingAttributeValue(final String attrName, final String attrValue,
                                                          final Map<String, List<Object>> attributes,
                                                          final boolean matchIfNoValueProvided) {
        LOGGER.debug("Locating matching attribute [{}] with value [{}] amongst the attribute collection [{}]", attrName, attrValue, attributes);
        if (StringUtils.isBlank(attrName)) {
            LOGGER.debug("Failed to match since attribute name is undefined");
            return false;
        }

        val names = attributes.entrySet()
            .stream()
            .filter(e -> {
                LOGGER.debug("Attempting to match [{}] against [{}]", attrName, e.getKey());
                return e.getKey().matches(attrName);
            })
            .collect(Collectors.toSet());

        LOGGER.debug("Found [{}] attributes relevant for multifactor authentication bypass", names.size());

        if (names.isEmpty()) {
            return false;
        }

        if (StringUtils.isBlank(attrValue)) {
            LOGGER.debug("No attribute value to match is provided; Match result is set to [{}]", matchIfNoValueProvided);
            return matchIfNoValueProvided;
        }

        val values = names
            .stream()
            .filter(e -> {
                val valuesCol = CollectionUtils.toCollection(e.getValue());
                LOGGER.debug("Matching attribute [{}] with values [{}] against [{}]", e.getKey(), valuesCol, attrValue);
                return valuesCol
                    .stream()
                    .anyMatch(v -> v.toString().matches(attrValue));
            }).collect(Collectors.toSet());

        LOGGER.debug("Matching attribute values remaining are [{}]", values);
        return !values.isEmpty();
    }


}
