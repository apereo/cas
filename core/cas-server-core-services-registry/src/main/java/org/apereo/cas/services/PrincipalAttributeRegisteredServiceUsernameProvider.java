package org.apereo.cas.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.TreeMap;

/**
 * Determines the username for this registered service based on a principal attribute.
 * If the attribute is not found, default principal id is returned.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class PrincipalAttributeRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAttributeProvider {

    private static final long serialVersionUID = -3546719400741715137L;

    private static final Logger LOGGER = LoggerFactory.getLogger(PrincipalAttributeRegisteredServiceUsernameProvider.class);

    private String usernameAttribute;

    /**
     * Private constructor to get around serialization issues.
     */
    private PrincipalAttributeRegisteredServiceUsernameProvider() {
        this.usernameAttribute = null;
    }

    /**
     * Instantiates a new default registered service username provider.
     *
     * @param usernameAttribute the username attribute
     */
    public PrincipalAttributeRegisteredServiceUsernameProvider(final String usernameAttribute) {
        this.usernameAttribute = usernameAttribute;
    }

    public PrincipalAttributeRegisteredServiceUsernameProvider(final String usernameAttribute, final String canonicalizationMode) {
        super(canonicalizationMode);
        this.usernameAttribute = usernameAttribute;
    }

    public String getUsernameAttribute() {
        return this.usernameAttribute;
    }

    @Override
    public String resolveUsernameInternal(final Principal principal, final Service service) {
        String principalId = principal.getId();

        final Map<String, Object> originalPrincipalAttributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        originalPrincipalAttributes.putAll(principal.getAttributes());

        final Map<String, Object> attributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        attributes.putAll(getPrincipalAttributes(principal, service));

        LOGGER.debug("Principal attributes available for selection of username attribute [{}] are [{}].", this.usernameAttribute, attributes);

        if (attributes.containsKey(this.usernameAttribute)) {
            principalId = attributes.get(this.usernameAttribute).toString();
        } else if (originalPrincipalAttributes.containsKey(this.usernameAttribute)) {
            LOGGER.warn("The selected username attribute [{}] was retrieved as a direct "
                            + "principal attributes and not through the attribute release policy for service [{}]. "
                            + "CAS is unable to detect new attribute values for [{}] after authentication unless the attribute "
                            + "is explicitly authorized for release via the service attribute release policy.",
                    this.usernameAttribute, service, this.usernameAttribute);
            principalId = originalPrincipalAttributes.get(this.usernameAttribute).toString();
        } else {
            LOGGER.warn("Principal [{}] does not have an attribute [{}] among attributes [{}] so CAS cannot "
                            + "provide the user attribute the service expects. "
                            + "CAS will instead return the default principal id [{}]. Ensure the attribute selected as the username "
                            + "is allowed to be released by the service attribute release policy.",
                    principalId,
                    this.usernameAttribute,
                    attributes,
                    principalId);
        }

        LOGGER.debug("Principal id to return for [{}] is [{}]. The default principal id is [{}].",
                service.getId(), principalId, principal.getId());
        return principalId.trim();
    }

    @Override
    public String toString() {
        final ToStringBuilder toStringBuilder = new ToStringBuilder(null, ToStringStyle.SHORT_PREFIX_STYLE);
        toStringBuilder.append("usernameAttribute", this.usernameAttribute);
        return toStringBuilder.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final PrincipalAttributeRegisteredServiceUsernameProvider rhs =
                (PrincipalAttributeRegisteredServiceUsernameProvider) obj;
        return new EqualsBuilder()
                .append(this.usernameAttribute, rhs.usernameAttribute)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.usernameAttribute)
                .toHashCode();
    }

    /**
     * Gets principal attributes. Will attempt to locate the principal
     * attribute repository from the context if one is defined to use
     * that instance to locate attributes. If none is available,
     * will use the default principal attributes.
     *
     * @param p       the principal
     * @param service the service
     * @return the principal attributes
     */
    protected Map<String, Object> getPrincipalAttributes(final Principal p, final Service service) {
        final ApplicationContext context = ApplicationContextProvider.getApplicationContext();
        if (context != null) {
            LOGGER.debug("Located application context to locate the service registry entry");
            final ServicesManager servicesManager = context.getBean(ServicesManager.class);
            if (servicesManager != null) {
                final RegisteredService registeredService = servicesManager.findServiceBy(service);

                if (registeredService != null && registeredService.getAccessStrategy().isServiceAccessAllowed()) {
                    LOGGER.debug("Located service [{}] in the registry. Attempting to resolve attributes for [{}]",
                            registeredService, p.getId());

                    if (registeredService.getAttributeReleasePolicy() == null) {
                        LOGGER.debug("No attribute release policy is defined for [{}]. Returning default principal attributes", service.getId());
                        return p.getAttributes();
                    }
                    return registeredService.getAttributeReleasePolicy().getAttributes(p, service, registeredService);
                }
            }

            LOGGER.debug("Could not locate service [{}] in the registry.", service.getId());
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE);
        }
        LOGGER.warn("No application context could be detected. Returning default principal attributes");
        return p.getAttributes();
    }

    public void setUsernameAttribute(final String usernameAttribute) {
        this.usernameAttribute = usernameAttribute;
    }
}
