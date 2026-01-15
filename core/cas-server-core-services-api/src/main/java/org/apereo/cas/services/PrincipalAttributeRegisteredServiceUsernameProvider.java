package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * Determines the username for this registered service based on a principal attribute.
 * If the attribute is not found, default principal id is returned.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class PrincipalAttributeRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAttributeProvider {

    @Serial
    private static final long serialVersionUID = -3546719400741715137L;

    private String usernameAttribute;

    @Override
    public String resolveUsernameInternal(final RegisteredServiceUsernameProviderContext context) {
        var principalId = context.getPrincipal().getId();
        val originalPrincipalAttributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        originalPrincipalAttributes.putAll(context.getPrincipal().getAttributes());
        LOGGER.debug("Original principal attributes available for selection of username attribute(s) [{}] are [{}].",
            usernameAttribute, originalPrincipalAttributes);
        val releasePolicyAttributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        FunctionUtils.doIfNull(context.getReleasingAttributes(),
            _ -> releasePolicyAttributes.putAll(getPrincipalAttributesFromReleasePolicy(context)),
            releasePolicyAttributes::putAll);

        LOGGER.debug("Attributes resolved by the release policy available for selection of username attribute(s) [{}] are [{}].",
            usernameAttribute, releasePolicyAttributes);

        if (StringUtils.isBlank(usernameAttribute)) {
            LOGGER.warn("No username attribute is defined for service [{}]. CAS will fall back onto using the default principal id. "
                    + "This is likely a mistake in the configuration of the registered service definition.",
                context.getRegisteredService().getName());
            return principalId.trim();
        }
        val definedUsernameAttributes = org.springframework.util.StringUtils.commaDelimitedListToSet(usernameAttribute);
        for (val attribute : definedUsernameAttributes) {
            val effectiveAttribute = attribute.trim();
            if (releasePolicyAttributes.containsKey(effectiveAttribute)) {
                LOGGER.debug("Attribute release policy for registered service [{}] contains an attribute for [{}]",
                    context.getRegisteredService().getServiceId(), effectiveAttribute);
                val value = releasePolicyAttributes.get(effectiveAttribute);
                return CollectionUtils.wrap(value).getFirst().toString();
            }

            if (originalPrincipalAttributes.containsKey(effectiveAttribute)) {
                LOGGER.debug("The selected username attribute [{}] was retrieved as a direct "
                        + "principal attribute and not through the attribute release policy for service [{}]. "
                        + "CAS is unable to detect new attribute values for [{}] after authentication unless the attribute "
                        + "is explicitly authorized for release via the service attribute release policy.",
                    effectiveAttribute, context.getRegisteredService(), effectiveAttribute);
                val value = originalPrincipalAttributes.get(effectiveAttribute);
                return CollectionUtils.wrap(value).getFirst().toString();
            }
        }
        LOGGER.info("Principal [{}] does not have an attribute [{}] among attributes [{}] so CAS cannot "
            + "provide the user attribute the service expects. "
            + "CAS will instead return the default principal id [{}]. Ensure the attribute selected as the username "
            + "is allowed to be released by the service attribute release policy.",
            principalId, usernameAttribute, releasePolicyAttributes, principalId);
        LOGGER.debug("Principal id to return for [{}] is [{}]. The default principal id is [{}].", context.getService().getId(),
            principalId, context.getPrincipal().getId());
        return principalId.trim();
    }

    protected Map<String, List<Object>> getPrincipalAttributesFromReleasePolicy(
        final RegisteredServiceUsernameProviderContext context) throws Throwable {
        if (context.getRegisteredService() != null && context.getRegisteredService().getAccessStrategy()
            .isServiceAccessAllowed(context.getRegisteredService(), context.getService())) {
            LOGGER.debug("Located service [{}] in the registry. Attempting to resolve attributes for [{}]",
                context.getRegisteredService(), context.getPrincipal().getId());
            if (context.getRegisteredService().getAttributeReleasePolicy() == null) {
                LOGGER.debug("No attribute release policy is defined for [{}]. Returning default principal attributes", context.getService().getId());
                return context.getPrincipal().getAttributes();
            }
            val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(context.getRegisteredService())
                .service(context.getService())
                .principal(context.getPrincipal())
                .applicationContext(context.getApplicationContext())
                .build();
            return context.getRegisteredService().getAttributeReleasePolicy().getAttributes(releasePolicyContext);
        }
        LOGGER.debug("Could not locate service [{}] in the registry.", context.getService().getId());
        throw UnauthorizedServiceException.denied("Rejected: %s".formatted(context.getService().getId()));
    }
}
