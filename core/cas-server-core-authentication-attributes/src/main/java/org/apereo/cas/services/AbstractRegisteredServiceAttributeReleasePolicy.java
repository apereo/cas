package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.PostLoad;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
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
    public Map<String, List<Object>> getAttributes(final RegisteredServiceAttributeReleasePolicyContext context) {
        val attributesToRelease = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        if (supports(context)) {
            LOGGER.debug("Initiating attributes release phase via [{}] for principal [{}] "
                         + "accessing service [{}] defined by registered service [{}]...",
                getClass().getSimpleName(), context.getPrincipal().getId(),
                context.getService(), context.getRegisteredService().getServiceId());

            val principalAttributes = resolveAttributesFromPrincipalAttributeRepository(context.getPrincipal(), context.getRegisteredService());
            LOGGER.debug("Found principal attributes [{}] for [{}]", principalAttributes, context.getPrincipal().getId());

            val availableAttributes = resolveAttributesFromAttributeDefinitionStore(context, principalAttributes);
            LOGGER.trace("Resolved principal attributes [{}] for [{}] from attribute definition store",
                availableAttributes, context.getPrincipal().getId());

            getRegisteredServicePrincipalAttributesRepository()
                .ifPresent(repository -> repository.update(context.getPrincipal().getId(), availableAttributes, context.getRegisteredService()));
            LOGGER.trace("Updating principal attributes repository cache for [{}] with [{}]", context.getPrincipal().getId(), availableAttributes);

            LOGGER.trace("Calling attribute policy [{}] to process attributes for [{}]", getClass().getSimpleName(), context.getPrincipal().getId());
            val policyAttributes = getAttributesInternal(context, availableAttributes);
            LOGGER.debug("Attribute policy [{}] allows release of [{}] for [{}]",
                getClass().getSimpleName(), policyAttributes, context.getPrincipal().getId());

            LOGGER.trace("Attempting to merge policy attributes and default attributes");
            if (isExcludeDefaultAttributes()) {
                LOGGER.debug("Ignoring default attribute policy attributes");
            } else {
                LOGGER.trace("Checking default attribute policy attributes");
                val defaultAttributes = getReleasedByDefaultAttributes(context.getPrincipal(), availableAttributes);
                LOGGER.debug("Default attributes found to be released are [{}]", defaultAttributes);
                if (!defaultAttributes.isEmpty()) {
                    LOGGER.debug("Adding default attributes first to the released set of attributes");
                    attributesToRelease.putAll(defaultAttributes);
                }
            }
            LOGGER.trace("Adding policy attributes to the released set of attributes");
            attributesToRelease.putAll(policyAttributes);
            insertPrincipalIdAsAttributeIfNeeded(context.getPrincipal(), attributesToRelease, context.getService(), context.getRegisteredService());
            if (getAttributeFilter() != null) {
                LOGGER.debug("Invoking attribute filter [{}] on the final set of attributes", getAttributeFilter());
                return getAttributeFilter().filter(attributesToRelease);
            }
            LOGGER.debug("Finalizing attributes release phase for principal [{}] accessing service [{}] defined by registered service [{}]...",
                context.getPrincipal().getId(), context.getService(), context.getRegisteredService().getServiceId());
            return returnFinalAttributesCollection(attributesToRelease, context.getRegisteredService());
        }
        return attributesToRelease;
    }

    @Override
    public Map<String, List<Object>> getConsentableAttributes(final RegisteredServiceAttributeReleasePolicyContext context) {
        val attributes = getAttributes(context);
        LOGGER.debug("Initial set of consentable attributes are [{}]", attributes);
        if (this.consentPolicy != null) {
            LOGGER.debug("Activating consent policy [{}] for service [{}]", this.consentPolicy, context.getService());
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
            LOGGER.debug("No consent policy is defined for service [{}]. "
                         + "Using the collection of attributes released for consent", context.getService());
        }
        LOGGER.debug("Finalized set of consentable attributes are [{}]", attributes);
        return attributes;
    }

    /**
     * Gets the attributes internally from the implementation.
     *
     * @param context    the context
     * @param attributes the principal attributes
     * @return the attributes allowed for release
     */
    public abstract Map<String, List<Object>> getAttributesInternal(
        RegisteredServiceAttributeReleasePolicyContext context,
        Map<String, List<Object>> attributes);

    /**
     * Supports this policy request..
     *
     * @param context the context
     * @return the boolean
     */
    protected boolean supports(final RegisteredServiceAttributeReleasePolicyContext context) {
        return true;
    }

    /**
     * Resolve attributes from attribute definition store and provide map.
     *
     * @param context             the context
     * @param principalAttributes the principal attributes
     * @return the map
     */
    protected Map<String, List<Object>> resolveAttributesFromAttributeDefinitionStore(
        final RegisteredServiceAttributeReleasePolicyContext context,
        final Map<String, List<Object>> principalAttributes) {
        LOGGER.trace("Retrieving attribute definition store and attribute definitions...");
        return ApplicationContextProvider.getAttributeDefinitionStore()
            .map(attributeDefinitionStore -> {
                if (attributeDefinitionStore.isEmpty()) {
                    LOGGER.trace("No attribute definitions are defined in the attribute definition "
                                 + "store, or no attribute definitions are requested.");
                    return principalAttributes;
                }
                val requestedDefinitions = new ArrayList<>(determineRequestedAttributeDefinitions(context));
                requestedDefinitions.addAll(principalAttributes.keySet());

                val availableAttributes = new LinkedHashMap<>(principalAttributes);
                availableAttributes.putAll(context.getReleasingAttributes());

                LOGGER.debug("Finding requested attribute definitions [{}] based on available attributes [{}]",
                    requestedDefinitions, availableAttributes);
                return attributeDefinitionStore.resolveAttributeValues(requestedDefinitions,
                    availableAttributes, context.getRegisteredService());
            })
            .orElseGet(() -> {
                LOGGER.trace("No attribute definition store is available in application context");
                return principalAttributes;
            });
    }

    /**
     * Resolve attributes from principal attribute repository.
     *
     * @param principal         the principal
     * @param registeredService the registered service
     * @return the map
     */
    protected Map<String, List<Object>> resolveAttributesFromPrincipalAttributeRepository(final Principal principal,
                                                                                          final RegisteredService registeredService) {
        val attributes = getRegisteredServicePrincipalAttributesRepository()
            .map(repository -> {
                LOGGER.debug("Using principal attribute repository [{}] to retrieve attributes", repository);
                return repository.getAttributes(principal, registeredService);
            })
            .orElseGet(principal::getAttributes);
        LOGGER.debug("Attributes retrieved from principal attribute repository for [{}] are [{}]", principal.getId(), attributes);
        return attributes;
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
            val usernameProvider = registeredService.getUsernameAttributeProvider();
            if (usernameProvider != null) {
                val id = usernameProvider.resolveUsername(principal, service, registeredService);
                LOGGER.debug("Releasing resolved principal id [{}] as attribute [{}]", id, getPrincipalIdAttribute());
                attributesToRelease.put(getPrincipalIdAttribute(), CollectionUtils.wrapList(principal.getId()));
            }
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
    protected Map<String, List<Object>> returnFinalAttributesCollection(final Map<String, List<Object>> attributesToRelease,
                                                                        final RegisteredService service) {
        LOGGER.debug("Final collection of attributes allowed are: [{}]", attributesToRelease);
        return attributesToRelease;
    }

    /**
     * Determines a default bundle of attributes that may be released to all services
     * without the explicit mapping for each service.
     *
     * @param principal  the principal
     * @param attributes the attributes
     * @return the released by default attributes
     */
    protected Map<String, List<Object>> getReleasedByDefaultAttributes(final Principal principal, final Map<String, List<Object>> attributes) {
        return ApplicationContextProvider.getCasConfigurationProperties()
            .map(properties -> {
                val defaultAttrs = properties.getAuthn().getAttributeRepository().getCore().getDefaultAttributesToRelease();
                LOGGER.debug("Default attributes for release are: [{}]", defaultAttrs);
                val defaultAttributesToRelease = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
                defaultAttrs.forEach(key -> {
                    if (attributes.containsKey(key)) {
                        LOGGER.debug("Found and added default attribute for release: [{}]", key);
                        defaultAttributesToRelease.put(key, attributes.get(key));
                    }
                });
                return defaultAttributesToRelease;
            })
            .orElseGet(TreeMap::new);
    }

    /**
     * This method should be overridden by release policies that are able to request definitions by listing them as being
     * released in the policy.  This method should return the list of definitions keys that need to be resolved by the
     * definition store so the can be resolved and released to the client.
     *
     * @param context the context
     * @return - List of requested attribute definitions to be released.
     */
    protected List<String> determineRequestedAttributeDefinitions(final RegisteredServiceAttributeReleasePolicyContext context) {
        return new ArrayList<>();
    }

    private Optional<RegisteredServicePrincipalAttributesRepository> getRegisteredServicePrincipalAttributesRepository() {
        return Optional.ofNullable(principalAttributesRepository)
            .or(ApplicationContextProvider::getPrincipalAttributesRepository);
    }
}
