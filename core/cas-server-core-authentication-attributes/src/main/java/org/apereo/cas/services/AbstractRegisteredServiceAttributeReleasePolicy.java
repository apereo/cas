package org.apereo.cas.services;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;

import javax.persistence.PostLoad;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Abstract release policy for attributes, provides common shared settings such as loggers and attribute filter config.
 * Subclasses are to provide the behavior for attribute retrieval.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public abstract class AbstractRegisteredServiceAttributeReleasePolicy implements RegisteredServiceAttributeReleasePolicy, Serializable {

    private static final long serialVersionUID = 5325460875620586503L;

    private RegisteredServiceAttributeFilter attributeFilter;

    private PrincipalAttributesRepository principalAttributesRepository = new DefaultPrincipalAttributesRepository();

    private RegisteredServiceConsentPolicy consentPolicy = new DefaultRegisteredServiceConsentPolicy();

    private boolean authorizedToReleaseCredentialPassword;

    private boolean authorizedToReleaseProxyGrantingTicket;

    private boolean excludeDefaultAttributes;

    private boolean authorizedToReleaseAuthenticationAttributes = true;

    private String principalIdAttribute;

    /**
     * Post load, after having loaded the bean via JPA, etc.
     */
    @PostLoad
    public void postLoad() {
        if (principalAttributesRepository == null) {
            this.principalAttributesRepository = new DefaultPrincipalAttributesRepository();
        }
        if (consentPolicy == null) {
            this.consentPolicy = new DefaultRegisteredServiceConsentPolicy();
        }
    }

    @Override
    public Map<String, Object> getConsentableAttributes(final Principal p, final Service selectedService, final RegisteredService service) {
        if (this.consentPolicy != null && !this.consentPolicy.isEnabled()) {
            LOGGER.debug("Consent is disabled for service [{}]", service);
            return new LinkedHashMap<>(0);
        }
        final Map<String, Object> attributes = getAttributes(p, selectedService, service);
        LOGGER.debug("Initial set of consentable attributes are [{}]", attributes);
        if (this.consentPolicy != null) {
            LOGGER.debug("Activating consent policy [{}] for service [{}]", this.consentPolicy, service);
            if (consentPolicy.getExcludedAttributes() != null && !consentPolicy.getExcludedAttributes().isEmpty()) {
                consentPolicy.getExcludedAttributes().forEach(attributes::remove);
                LOGGER.debug("Consentable attributes after removing excluded attributes are [{}]", attributes);
            } else {
                LOGGER.debug("No attributes are defined per the consent policy to be excluded from the consentable attributes");
            }
            if (consentPolicy.getIncludeOnlyAttributes() != null && !consentPolicy.getIncludeOnlyAttributes().isEmpty()) {
                attributes.keySet().retainAll(consentPolicy.getIncludeOnlyAttributes());
                LOGGER.debug("Consentable attributes after force-including attributes are [{}]", attributes);
            } else {
                LOGGER.debug("No attributes are defined per the consent policy to forcefully be included in the consentable attributes");
            }
        } else {
            LOGGER.debug("No consent policy is defined for service [{}]. Using the collection of attributes released for consent", service);
        }
        LOGGER.debug("Finalized set of consentable attributes are [{}]", attributes);
        return attributes;
    }

    @Override
    public Map<String, Object> getAttributes(final Principal principal, final Service selectedService, final RegisteredService registeredService) {
        LOGGER.debug("Initiating attributes release phase for principal [{}] accessing service [{}] defined by registered service [{}]...",
            principal.getId(), selectedService.getId(), registeredService.getServiceId());
        LOGGER.debug("Locating principal attributes for [{}]", principal.getId());
        final Map<String, Object> principalAttributes = resolveAttributesFromPrincipalAttributeRepository(principal);
        LOGGER.debug("Found principal attributes [{}] for [{}]", principalAttributes, principal.getId());
        LOGGER.debug("Calling attribute policy [{}] to process attributes for [{}]", getClass().getSimpleName(), principal.getId());
        final Map<String, Object> policyAttributes = getAttributesInternal(principal, principalAttributes, registeredService);
        LOGGER.debug("Attribute policy [{}] allows release of [{}] for [{}]", getClass().getSimpleName(), policyAttributes, principal.getId());
        LOGGER.debug("Attempting to merge policy attributes and default attributes");
        final Map<String, Object> attributesToRelease = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (isExcludeDefaultAttributes()) {
            LOGGER.debug("Ignoring default attribute policy attributes");
        } else {
            LOGGER.debug("Checking default attribute policy attributes");
            final Map<String, Object> defaultAttributes = getReleasedByDefaultAttributes(principal, principalAttributes);
            LOGGER.debug("Default attributes found to be released are [{}]", defaultAttributes);
            LOGGER.debug("Adding default attributes first to the released set of attributes");
            attributesToRelease.putAll(defaultAttributes);
        }
        LOGGER.debug("Adding policy attributes to the released set of attributes");
        attributesToRelease.putAll(policyAttributes);
        insertPrincipalIdAsAttributeIfNeeded(principal, attributesToRelease, selectedService, registeredService);
        if (getAttributeFilter() != null) {
            LOGGER.debug("Invoking attribute filter [{}] on the final set of attributes", getAttributeFilter());
            return getAttributeFilter().filter(attributesToRelease);
        }
        LOGGER.debug("Finalizing attributes release phase for principal [{}] accessing service [{}] defined by registered service [{}]...",
            principal.getId(), selectedService.getId(), registeredService.getServiceId());
        return returnFinalAttributesCollection(attributesToRelease, registeredService);
    }

    /**
     * Resolve attributes from principal attribute repository.
     *
     * @param principal the principal
     * @return the map
     */
    protected Map<String, Object> resolveAttributesFromPrincipalAttributeRepository(final Principal principal) {
        PrincipalAttributesRepository repository = getPrincipalAttributesRepository();
        if (repository == null) {
            LOGGER.debug("No principal attribute repository is defined for the service. Evaluating global attribute caching policy...");
            final ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
            if (applicationContext != null) {
                if (applicationContext.containsBean("globalPrincipalAttributeRepository")) {
                    LOGGER.debug("Loading global principal attribute repository with caching policies...");
                    repository = applicationContext.getBean("globalPrincipalAttributeRepository", PrincipalAttributesRepository.class);
                } else {
                    LOGGER.warn("No global principal attribute repository can be located from the application context.");
                }
            }
        }
        if (repository != null) {
            LOGGER.debug("Using principal attribute repository [{}] to retrieve attributes", repository);
            return repository.getAttributes(principal);
        }
        return principal.getAttributes();
    }

    /**
     * Release principal id as attribute if needed.
     *
     * @param principal           the principal
     * @param attributesToRelease the attributes to release
     * @param service             the service
     * @param registeredService   the registered service
     */
    protected void insertPrincipalIdAsAttributeIfNeeded(final Principal principal, final Map<String, Object> attributesToRelease,
                                                        final Service service, final RegisteredService registeredService) {
        if (StringUtils.isNotBlank(getPrincipalIdAttribute())) {
            LOGGER.debug("Attempting to resolve the principal id for service [{}]", registeredService.getServiceId());
            final String id = registeredService.getUsernameAttributeProvider().resolveUsername(principal, service, registeredService);
            LOGGER.debug("Releasing resolved principal id [{}] as attribute [{}]", id, getPrincipalIdAttribute());
            attributesToRelease.put(getPrincipalIdAttribute(), principal.getId());
        }
    }

    /**
     * Return the final attributes collection.
     * Subclasses may override this minute to impose last minute rules.
     *
     * @param attributesToRelease the attributes to release
     * @param service             the service
     * @return the map
     */
    protected Map<String, Object> returnFinalAttributesCollection(final Map<String, Object> attributesToRelease, final RegisteredService service) {
        LOGGER.debug("Final collection of attributes allowed are: [{}]", attributesToRelease);
        return attributesToRelease;
    }

    /**
     * Determines a default bundle of attributes that may be released to all services
     * without the explicit mapping for each service.
     *
     * @param p          the principal
     * @param attributes the attributes
     * @return the released by default attributes
     */
    protected Map<String, Object> getReleasedByDefaultAttributes(final Principal p, final Map<String, Object> attributes) {
        final ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
        if (ctx != null) {
            LOGGER.debug("Located application context. Retrieving default attributes for release, if any");
            final CasConfigurationProperties props = ctx.getAutowireCapableBeanFactory().getBean(CasConfigurationProperties.class);
            final Set<String> defaultAttrs = props.getAuthn().getAttributeRepository().getDefaultAttributesToRelease();
            LOGGER.debug("Default attributes for release are: [{}]", defaultAttrs);
            final Map<String, Object> defaultAttributesToRelease = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            defaultAttrs.stream().forEach(key -> {
                if (attributes.containsKey(key)) {
                    LOGGER.debug("Found and added default attribute for release: [{}]", key);
                    defaultAttributesToRelease.put(key, attributes.get(key));
                }
            });
            return defaultAttributesToRelease;
        }
        return new TreeMap<>();
    }

    /**
     * Gets the attributes internally from the implementation.
     *
     * @param principal  the principal
     * @param attributes the principal attributes
     * @param service    the service
     * @return the attributes allowed for release
     */
    public abstract Map<String, Object> getAttributesInternal(Principal principal, Map<String, Object> attributes, RegisteredService service);

}
