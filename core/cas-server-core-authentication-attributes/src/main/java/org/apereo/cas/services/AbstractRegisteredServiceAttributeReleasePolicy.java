package org.apereo.cas.services;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.PostLoad;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractRegisteredServiceAttributeReleasePolicy implements RegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = 5325460875620586503L;

    private RegisteredServiceAttributeFilter attributeFilter;

    private RegisteredServicePrincipalAttributesRepository principalAttributesRepository = new DefaultPrincipalAttributesRepository();

    private RegisteredServiceConsentPolicy consentPolicy = new DefaultRegisteredServiceConsentPolicy();

    private boolean authorizedToReleaseCredentialPassword;

    private boolean authorizedToReleaseProxyGrantingTicket;

    private boolean excludeDefaultAttributes;

    private boolean authorizedToReleaseAuthenticationAttributes = true;

    private String principalIdAttribute;

    private int order;

    private static RegisteredServicePrincipalAttributesRepository getPrincipalAttributesRepositoryFromApplicationContext() {
        val applicationContext = ApplicationContextProvider.getConfigurableApplicationContext();
        if (applicationContext != null && applicationContext.isActive()) {
            if (applicationContext.containsBean("globalPrincipalAttributeRepository")) {
                LOGGER.trace("Loading global principal attribute repository with caching policies...");
                return applicationContext.getBean("globalPrincipalAttributeRepository", RegisteredServicePrincipalAttributesRepository.class);
            }
            LOGGER.warn("No global principal attribute repository can be located from the application context.");
        }
        return null;
    }

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
    public Map<String, List<Object>> getAttributes(final Principal principal, final Service selectedService, final RegisteredService registeredService) {
        LOGGER.debug("Initiating attributes release phase for principal [{}] accessing service [{}] defined by registered service [{}]...",
            principal.getId(), selectedService, registeredService.getServiceId());
        LOGGER.trace("Locating principal attributes for [{}]", principal.getId());

        val principalAttributes = resolveAttributesFromPrincipalAttributeRepository(principal, registeredService);
        LOGGER.debug("Found principal attributes [{}] for [{}]", principalAttributes, principal.getId());

        val attributesFromDefinitions = resolveAttributesFromAttributeDefinitionStore(principal, principalAttributes, registeredService, selectedService);
        LOGGER.trace("Resolved principal attributes [{}] for [{}] from attribute definition store", attributesFromDefinitions, principal.getId());

        LOGGER.trace("Calling attribute policy [{}] to process attributes for [{}]", getClass().getSimpleName(), principal.getId());
        val policyAttributes = getAttributesInternal(principal, attributesFromDefinitions, registeredService, selectedService);
        LOGGER.debug("Attribute policy [{}] allows release of [{}] for [{}]", getClass().getSimpleName(), policyAttributes, principal.getId());

        LOGGER.trace("Attempting to merge policy attributes and default attributes");
        val attributesToRelease = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        if (isExcludeDefaultAttributes()) {
            LOGGER.debug("Ignoring default attribute policy attributes");
        } else {
            LOGGER.trace("Checking default attribute policy attributes");
            val defaultAttributes = getReleasedByDefaultAttributes(principal, attributesFromDefinitions);
            LOGGER.debug("Default attributes found to be released are [{}]", defaultAttributes);
            if (!defaultAttributes.isEmpty()) {
                LOGGER.debug("Adding default attributes first to the released set of attributes");
                attributesToRelease.putAll(defaultAttributes);
            }
        }
        LOGGER.trace("Adding policy attributes to the released set of attributes");
        attributesToRelease.putAll(policyAttributes);
        insertPrincipalIdAsAttributeIfNeeded(principal, attributesToRelease, selectedService, registeredService);
        if (getAttributeFilter() != null) {
            LOGGER.debug("Invoking attribute filter [{}] on the final set of attributes", getAttributeFilter());
            return getAttributeFilter().filter(attributesToRelease);
        }
        LOGGER.debug("Finalizing attributes release phase for principal [{}] accessing service [{}] defined by registered service [{}]...",
            principal.getId(), selectedService, registeredService.getServiceId());
        return returnFinalAttributesCollection(attributesToRelease, registeredService);
    }

    @Override
    public Map<String, List<Object>> getConsentableAttributes(final Principal p, final Service selectedService, final RegisteredService service) {
        if (this.consentPolicy != null && !this.consentPolicy.isEnabled()) {
            LOGGER.debug("Consent is disabled for service [{}]", service);
            return new LinkedHashMap<>(0);
        }
        val attributes = getAttributes(p, selectedService, service);
        LOGGER.debug("Initial set of consentable attributes are [{}]", attributes);
        if (this.consentPolicy != null) {
            LOGGER.debug("Activating consent policy [{}] for service [{}]", this.consentPolicy, service);
            val excludedAttributes = consentPolicy.getExcludedAttributes();
            if (excludedAttributes != null && !excludedAttributes.isEmpty()) {
                excludedAttributes.forEach(attributes::remove);
                LOGGER.debug("Consentable attributes after removing excluded attributes are [{}]", attributes);
            } else {
                LOGGER.debug("No attributes are defined per the consent policy to be excluded from the consentable attributes");
            }
            val includeOnlyAttributes = consentPolicy.getIncludeOnlyAttributes();
            if (includeOnlyAttributes != null && !includeOnlyAttributes.isEmpty()) {
                attributes.keySet().retainAll(includeOnlyAttributes);
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

    protected Map<String, List<Object>> resolveAttributesFromAttributeDefinitionStore(final Principal principal,
                                                                                      final Map<String, List<Object>> principalAttributes,
                                                                                      final RegisteredService registeredService,
                                                                                      final Service selectedService) {
        val ctx = ApplicationContextProvider.getApplicationContext();
        if (ctx == null) {
            LOGGER.trace("No application context can be retrieved to locate attribute definition store");
            return principalAttributes;
        }
        LOGGER.trace("Located application context. Retrieving attribute definition store and attribute definitions...");
        val beanFactory = ctx.getAutowireCapableBeanFactory();
        if (!beanFactory.containsBean("attributeDefinitionStore")) {
            LOGGER.trace("No attribute definition store is available in application context");
            return principalAttributes;
        }
        val attributeDefinitionStore = beanFactory.getBean(AttributeDefinitionStore.class);
        if (attributeDefinitionStore.isEmpty()) {
            LOGGER.trace("No attribute definitions are defined in the attribute definition store");
            return principalAttributes;
        }
        return attributeDefinitionStore.resolveAttributeValues(principalAttributes, registeredService);
    }

    /**
     * Resolve attributes from principal attribute repository.
     *
     * @param principal         the principal
     * @param registeredService the registered service
     * @return the map
     */
    protected Map<String, List<Object>> resolveAttributesFromPrincipalAttributeRepository(final Principal principal, final RegisteredService registeredService) {
        val repository = ObjectUtils.defaultIfNull(this.principalAttributesRepository,
            getPrincipalAttributesRepositoryFromApplicationContext());
        if (repository != null) {
            LOGGER.debug("Using principal attribute repository [{}] to retrieve attributes", repository);
            return repository.getAttributes(principal, registeredService);
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
    protected void insertPrincipalIdAsAttributeIfNeeded(final Principal principal, final Map<String, List<Object>> attributesToRelease,
                                                        final Service service, final RegisteredService registeredService) {
        if (StringUtils.isNotBlank(getPrincipalIdAttribute())) {
            LOGGER.debug("Attempting to resolve the principal id for service [{}]", registeredService.getServiceId());
            val id = registeredService.getUsernameAttributeProvider().resolveUsername(principal, service, registeredService);
            LOGGER.debug("Releasing resolved principal id [{}] as attribute [{}]", id, getPrincipalIdAttribute());
            attributesToRelease.put(getPrincipalIdAttribute(), CollectionUtils.wrapList(principal.getId()));
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
    protected Map<String, List<Object>> returnFinalAttributesCollection(final Map<String, List<Object>> attributesToRelease, final RegisteredService service) {
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
    protected Map<String, List<Object>> getReleasedByDefaultAttributes(final Principal p, final Map<String, List<Object>> attributes) {
        val ctx = ApplicationContextProvider.getApplicationContext();
        if (ctx != null) {
            LOGGER.trace("Located application context. Retrieving default attributes for release, if any");
            val props = ctx.getAutowireCapableBeanFactory().getBean(CasConfigurationProperties.class);
            val defaultAttrs = props.getAuthn().getAttributeRepository().getDefaultAttributesToRelease();
            LOGGER.debug("Default attributes for release are: [{}]", defaultAttrs);
            val defaultAttributesToRelease = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
            defaultAttrs.forEach(key -> {
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
     * @param principal       the principal
     * @param attributes      the principal attributes
     * @param service         the service
     * @param selectedService the selected service
     * @return the attributes allowed for release
     */
    public abstract Map<String, List<Object>> getAttributesInternal(Principal principal, Map<String, List<Object>> attributes,
                                                                    RegisteredService service, Service selectedService);
}
