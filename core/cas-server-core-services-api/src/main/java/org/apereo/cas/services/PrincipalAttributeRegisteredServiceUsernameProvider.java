package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrincipalAttributeRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAttributeProvider {

    private static final long serialVersionUID = -3546719400741715137L;

    private String usernameAttribute;

    public PrincipalAttributeRegisteredServiceUsernameProvider(final String usernameAttribute, final String canonicalizationMode) {
        super(canonicalizationMode, false);
        this.usernameAttribute = usernameAttribute;
    }

    @Override
    public String resolveUsernameInternal(final Principal principal, final Service service, final RegisteredService registeredService) {
        var principalId = principal.getId();
        val originalPrincipalAttributes = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
        originalPrincipalAttributes.putAll(principal.getAttributes());
        LOGGER.debug("Original principal attributes available for selection of username attribute [{}] are [{}].", this.usernameAttribute, originalPrincipalAttributes);
        val releasePolicyAttributes = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
        releasePolicyAttributes.putAll(getPrincipalAttributesFromReleasePolicy(principal, service, registeredService));
        LOGGER.debug("Attributes resolved by the release policy available for selection of username attribute [{}] are [{}].",
            this.usernameAttribute, releasePolicyAttributes);

        if (StringUtils.isBlank(this.usernameAttribute)) {
            LOGGER.warn("No username attribute is defined for service [{}]. CAS will fall back onto using the default principal id. "
                + "This is likely a mistake in the configuration of the registered service definition.", registeredService.getName());
        } else if (releasePolicyAttributes.containsKey(this.usernameAttribute)) {
            LOGGER.debug("Attribute release policy for registered service [{}] contains an attribute for [{}]",
                registeredService.getServiceId(), this.usernameAttribute);
            val value = releasePolicyAttributes.get(this.usernameAttribute);
            principalId = CollectionUtils.wrap(value).get(0).toString();
        } else if (originalPrincipalAttributes.containsKey(this.usernameAttribute)) {
            LOGGER.debug("The selected username attribute [{}] was retrieved as a direct "
                    + "principal attribute and not through the attribute release policy for service [{}]. "
                    + "CAS is unable to detect new attribute values for [{}] after authentication unless the attribute "
                    + "is explicitly authorized for release via the service attribute release policy.",
                this.usernameAttribute, service, this.usernameAttribute);
            val value = originalPrincipalAttributes.get(this.usernameAttribute);
            principalId = CollectionUtils.wrap(value).get(0).toString();
        } else {
            LOGGER.warn("Principal [{}] does not have an attribute [{}] among attributes [{}] so CAS cannot "
                + "provide the user attribute the service expects. "
                + "CAS will instead return the default principal id [{}]. Ensure the attribute selected as the username "
                + "is allowed to be released by the service attribute release policy.", principalId, this.usernameAttribute, releasePolicyAttributes, principalId);
        }
        LOGGER.debug("Principal id to return for [{}] is [{}]. The default principal id is [{}].", service.getId(), principalId, principal.getId());
        return principalId.trim();
    }

    /**
     * Gets principal attributes. Will attempt to locate the principal
     * attribute repository from the context if one is defined to use
     * that instance to locate attributes. If none is available,
     * will use the default principal attributes.
     *
     * @param p                 the principal
     * @param service           the service
     * @param registeredService the registered service
     * @return the principal attributes
     */
    protected Map<String, List<Object>> getPrincipalAttributesFromReleasePolicy(final Principal p, final Service service, final RegisteredService registeredService) {
        if (registeredService != null && registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            LOGGER.debug("Located service [{}] in the registry. Attempting to resolve attributes for [{}]", registeredService, p.getId());
            if (registeredService.getAttributeReleasePolicy() == null) {
                LOGGER.debug("No attribute release policy is defined for [{}]. Returning default principal attributes", service.getId());
                return p.getAttributes();
            }
            return registeredService.getAttributeReleasePolicy().getAttributes(p, service, registeredService);
        }
        LOGGER.debug("Could not locate service [{}] in the registry.", service.getId());
        throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
    }
}
