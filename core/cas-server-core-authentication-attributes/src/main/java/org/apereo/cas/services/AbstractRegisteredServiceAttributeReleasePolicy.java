package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.CaseCanonicalizationMode;
import org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;
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
import jakarta.persistence.PostLoad;

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

    @Serial
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

    private String canonicalizationMode = "NONE";

    private RegisteredServiceAttributeReleaseActivationCriteria activationCriteria;

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
        canonicalizationMode = StringUtils.defaultIfBlank(canonicalizationMode, "NONE");
    }

    @Override
    public Map<String, List<Object>> getAttributes(final RegisteredServiceAttributeReleasePolicyContext context) throws Throwable {
        val attributesToRelease = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        if (supports(context)) {
            LOGGER.debug("Initiating attributes release phase via [{}] for principal [{}] "
                    + "accessing service [{}] defined by registered service [{}]...",
                getClass().getSimpleName(), context.getPrincipal().getId(),
                context.getService(), context.getRegisteredService().getServiceId());

            val principalAttributes = resolveAttributesFromPrincipalAttributeRepository(context);
            LOGGER.debug("Found principal attributes [{}] for [{}]", principalAttributes, context.getPrincipal().getId());

            val availableAttributes = resolveAttributesFromAttributeDefinitionStore(context, principalAttributes);
            LOGGER.trace("Resolved principal attributes [{}] for [{}] from attribute definition store",
                availableAttributes, context.getPrincipal().getId());

            val repository = getRegisteredServicePrincipalAttributesRepository(context);
            LOGGER.trace("Updating principal attributes repository cache for [{}] with [{}]", context.getPrincipal().getId(), availableAttributes);
            repository.update(context.getPrincipal().getId(), availableAttributes, context);

            LOGGER.trace("Calling attribute policy [{}] to process attributes for [{}]",
                getClass().getSimpleName(), context.getPrincipal().getId());
            val policyAttributes = getAttributesInternal(context, availableAttributes);
            LOGGER.debug("Attribute policy [{}] allows release of [{}] for [{}]",
                getClass().getSimpleName(), policyAttributes, context.getPrincipal().getId());

            LOGGER.trace("Attempting to merge policy attributes and default attributes");
            if (isExcludeDefaultAttributes()) {
                LOGGER.debug("Ignoring default attribute policy attributes");
            } else {
                LOGGER.trace("Checking default attribute policy attributes");
                val defaultAttributes = getReleasedByDefaultAttributes(context, availableAttributes);
                LOGGER.debug("Default attributes found to be released are [{}]", defaultAttributes);
                if (!defaultAttributes.isEmpty()) {
                    LOGGER.debug("Adding default attributes first to the released set of attributes");
                    attributesToRelease.putAll(defaultAttributes);
                }
            }
            LOGGER.trace("Adding policy attributes to the released set of attributes");
            attributesToRelease.putAll(policyAttributes);
            insertPrincipalIdAsAttributeIfNeeded(context, attributesToRelease);
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
    public Map<String, List<Object>> getConsentableAttributes(final RegisteredServiceAttributeReleasePolicyContext context) throws Throwable {
        val attributes = getAttributes(context);
        LOGGER.debug("Initial set of consentable attributes are [{}]", attributes);

        val results = Optional.ofNullable(this.consentPolicy)
            .filter(policy -> Objects.nonNull(policy.getStatus()))
            .map(policy -> {
                if (policy.getStatus().isFalse()) {
                    LOGGER.debug("Consent policy is turned off and disabled for service [{}].", context.getService());
                    return new HashMap<String, List<Object>>();
                }
                if (policy.getStatus().isTrue()) {
                    if (policy.getExcludedServices() != null && policy.getExcludedServices().stream()
                        .anyMatch(ex -> RegexUtils.find(ex, context.getService().getId()))) {
                        LOGGER.debug("Consent policy will exclude service [{}].", context.getService());
                        return new HashMap<String, List<Object>>();
                    }

                    LOGGER.debug("Activating consent policy [{}] for service [{}]", policy, context.getService());
                    val excludedAttributes = policy.getExcludedAttributes();
                    if (excludedAttributes != null && !excludedAttributes.isEmpty()) {
                        excludedAttributes.forEach(attributes::remove);
                        LOGGER.debug("Consentable attributes after removing excluded attributes are [{}]", attributes);
                    } else {
                        LOGGER.debug("No attributes are defined per the consent policy to be excluded from the consentable attributes");
                    }
                    val includeOnlyAttributes = policy.getIncludeOnlyAttributes();
                    if (includeOnlyAttributes != null && !includeOnlyAttributes.isEmpty()) {
                        attributes.keySet().retainAll(includeOnlyAttributes);
                        LOGGER.debug("Consentable attributes after force-including attributes are [{}]", attributes);
                    } else {
                        LOGGER.debug("No attributes are defined per the consent policy to forcefully be included in the consentable attributes");
                    }
                }
                return attributes;
            })
            .orElseGet(() -> {
                LOGGER.debug("No consent policy is defined for service [{}]. "
                    + "Using the collection of attributes released for consent", context.getService());
                return attributes;
            });
        LOGGER.debug("Finalized set of consentable attributes are [{}]", results);
        return results;
    }

    protected abstract Map<String, List<Object>> getAttributesInternal(
        RegisteredServiceAttributeReleasePolicyContext context,
        Map<String, List<Object>> attributes) throws Throwable;

    protected boolean supports(final RegisteredServiceAttributeReleasePolicyContext context) {
        val criteria = getActivationCriteria();
        return criteria == null || criteria.shouldActivate(context);
    }

    protected Map<String, List<Object>> resolveAttributesFromAttributeDefinitionStore(
        final RegisteredServiceAttributeReleasePolicyContext context,
        final Map<String, List<Object>> principalAttributes) {
        if (context.getApplicationContext().containsBean(AttributeDefinitionStore.BEAN_NAME)) {
            LOGGER.trace("Retrieving attribute definition store and attribute definitions...");
            val definitionStore = context.getApplicationContext().getBean(AttributeDefinitionStore.BEAN_NAME, AttributeDefinitionStore.class);
            val availableAttributes = new LinkedHashMap<>(principalAttributes);
            availableAttributes.putAll(context.getReleasingAttributes());
            if (definitionStore.isEmpty()) {
                LOGGER.trace("No attribute definitions are defined in the attribute definition "
                    + "store, or no attribute definitions are requested.");
                return availableAttributes;
            }
            val requestedDefinitions = new LinkedHashSet<>(determineRequestedAttributeDefinitions(context));
            requestedDefinitions.addAll(principalAttributes.keySet());

            LOGGER.debug("Finding requested attribute definitions [{}] based on available attributes [{}]",
                requestedDefinitions, availableAttributes);
            return definitionStore.resolveAttributeValues(requestedDefinitions, availableAttributes,
                context.getPrincipal(), context.getRegisteredService(), context.getService());
        }
        LOGGER.trace("No attribute definition store is available in application context");
        return principalAttributes;
    }

    protected Map<String, List<Object>> resolveAttributesFromPrincipalAttributeRepository(final RegisteredServiceAttributeReleasePolicyContext context) {
        val repository = getRegisteredServicePrincipalAttributesRepository(context);
        LOGGER.debug("Using principal attribute repository [{}] to retrieve attributes", repository);
        val attributes = repository.getAttributes(context);
        LOGGER.debug("Attributes retrieved from principal attribute repository for [{}] are [{}]", context.getPrincipal().getId(), attributes);
        return attributes;
    }

    protected void insertPrincipalIdAsAttributeIfNeeded(final RegisteredServiceAttributeReleasePolicyContext context,
                                                        final Map<String, List<Object>> attributesToRelease) throws Throwable {
        if (StringUtils.isNotBlank(getPrincipalIdAttribute()) && !attributesToRelease.containsKey(getPrincipalIdAttribute())) {
            LOGGER.debug("Attempting to resolve the principal id for service [{}]", context.getRegisteredService().getServiceId());
            val usernameProvider = context.getRegisteredService().getUsernameAttributeProvider();
            if (usernameProvider != null) {
                val usernameContext = RegisteredServiceUsernameProviderContext.builder()
                    .service(context.getService())
                    .principal(context.getPrincipal())
                    .registeredService(context.getRegisteredService())
                    .releasingAttributes(attributesToRelease)
                    .applicationContext(context.getApplicationContext())
                    .build();
                val id = usernameProvider.resolveUsername(usernameContext);
                LOGGER.debug("Releasing resolved principal id [{}] as attribute [{}]", id, getPrincipalIdAttribute());
                attributesToRelease.put(getPrincipalIdAttribute(), CollectionUtils.wrapList(id));
            }
        }
    }

    protected Map<String, List<Object>> returnFinalAttributesCollection(final Map<String, List<Object>> attributesToRelease,
                                                                        final RegisteredService service) {
        LOGGER.debug("Final collection of attributes allowed are: [{}]", attributesToRelease);
        val transform = StringUtils.isBlank(canonicalizationMode)
            ? CaseCanonicalizationMode.NONE
            : CaseCanonicalizationMode.valueOf(this.canonicalizationMode);
        if (transform != CaseCanonicalizationMode.NONE) {
            val transformedAttributes = new HashMap<>(attributesToRelease);
            transformedAttributes.forEach((key, value) -> {
                val values = value
                    .stream()
                    .map(Object::toString)
                    .map(transform::canonicalize)
                    .toList();
                transformedAttributes.put(key, (List) values);
            });
            return transformedAttributes;
        }
        return attributesToRelease;
    }

    /**
     * Determines a default bundle of attributes that may be released to all services
     * without the explicit mapping for each service.
     *
     * @param context    the context
     * @param attributes the attributes
     * @return the released by default attributes
     */
    protected Map<String, List<Object>> getReleasedByDefaultAttributes(final RegisteredServiceAttributeReleasePolicyContext context,
                                                                       final Map<String, List<Object>> attributes) {
        val properties = context.getApplicationContext().getBean(CasConfigurationProperties.class);
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

    private RegisteredServicePrincipalAttributesRepository getRegisteredServicePrincipalAttributesRepository(
        final RegisteredServiceAttributeReleasePolicyContext context) {
        return Optional.ofNullable(principalAttributesRepository)
            .orElseGet(() -> context.getApplicationContext()
                .getBean(PrincipalResolver.BEAN_NAME_GLOBAL_PRINCIPAL_ATTRIBUTE_REPOSITORY, RegisteredServicePrincipalAttributesRepository.class));
    }
}
