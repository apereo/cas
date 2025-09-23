package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.attribute.AggregatingPersonAttributeDao;
import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.AttributeRepositoryQuery;
import org.apereo.cas.authentication.attribute.AttributeRepositoryResolver;
import org.apereo.cas.authentication.attribute.MergingPersonAttributeDaoImpl;
import org.apereo.cas.authentication.attribute.PrincipalAttributeRepositoryFetcher;
import org.apereo.cas.authentication.attribute.TenantPersonAttributeDaoBuilder;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.merger.AttributeMerger;
import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.transforms.ChainingPrincipalNameTransformer;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Resolves principals by querying a data source using the Person Directory API.
 * The {@link Principal#getAttributes()} are populated by the results of the
 * query and the principal ID may optionally be set by proving an attribute whose first non-null value is used;
 * otherwise the credential ID is used for the principal ID.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@ToString
@RequiredArgsConstructor
@Getter
@Setter
public class PersonDirectoryPrincipalResolver implements PrincipalResolver {

    private final PrincipalResolutionContext context;

    @Override
    public Principal resolve(final Credential credential, final Optional<Principal> currentPrincipal,
                             final Optional<AuthenticationHandler> handler, final Optional<Service> service) throws Throwable {

        LOGGER.trace("Attempting to resolve a principal via [{}]", getName());
        var principalId = extractPrincipalId(credential, currentPrincipal);
        if (StringUtils.isBlank(principalId)) {
            LOGGER.debug("Principal id [{}] could not be found", principalId);
            return null;
        }
        if (context.getPrincipalNameTransformer() != null) {
            principalId = context.getPrincipalNameTransformer().transform(principalId);
        }
        LOGGER.trace("Creating principal for [{}]", principalId);
        if (context.isResolveAttributes()) {
            val attributes = retrievePersonAttributes(principalId, credential,
                currentPrincipal, new HashMap<>(), service, handler);
            if (attributes == null || attributes.isEmpty()) {
                LOGGER.debug("Principal id [{}] did not specify any attributes", principalId);
                if (!context.isReturnNullIfNoAttributes()) {
                    val principal = buildResolvedPrincipal(principalId, new HashMap<>(), credential, currentPrincipal, handler);
                    LOGGER.debug("Returning the principal with id [{}] without any attributes", principal);
                    return principal;
                }
                LOGGER.debug("[{}] is configured to return null if no attributes are found for [{}]", getClass().getName(), principalId);
                return null;
            }
            LOGGER.debug("Retrieved [{}] attribute(s) from the repository", attributes.size());
            val result = convertPersonAttributesToPrincipal(principalId, currentPrincipal, attributes);
            if (!result.isSuccess() && context.isReturnNullIfNoAttributes()) {
                LOGGER.warn("Principal resolution is unable to produce a result and will return null");
                return null;
            }

            val principal = buildResolvedPrincipal(result.getPrincipalId(), result.getAttributes(),
                credential, currentPrincipal, handler);
            LOGGER.debug("Final resolved principal by [{}] is [{}]", getName(), principal);
            return principal;
        }
        val principal = buildResolvedPrincipal(principalId, new HashMap<>(),
            credential, currentPrincipal, handler);
        LOGGER.debug("Final resolved principal by [{}] without resolving attributes is [{}]", getName(), principal);
        return principal;
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null && credential.getId() != null;
    }

    protected Principal buildResolvedPrincipal(final String id, final Map<String, List<Object>> attributes,
                                               final Credential credential, final Optional<Principal> currentPrincipal,
                                               final Optional<AuthenticationHandler> handler) throws Throwable {
        return context.getPrincipalFactory().createPrincipal(id, attributes);
    }

    protected PrincipalResolutionResult convertPersonAttributesToPrincipal(final String extractedPrincipalId,
                                                                           final Optional<Principal> currentPrincipal,
                                                                           final Map<String, List<Object>> attributes) {
        val convertedAttributes = new LinkedHashMap<String, List<Object>>();
        attributes.forEach((key, attrValue) -> {
            val values = ((List<Object>) CollectionUtils.toCollection(attrValue, ArrayList.class))
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            LOGGER.debug("Found attribute [{}] with value(s) [{}]", key, values);
            convertedAttributes.put(key, values);
        });

        val builder = PrincipalResolutionResult.builder();
        var principalId = extractedPrincipalId;

        if (StringUtils.isNotBlank(context.getPrincipalAttributeNames())) {
            val attrNames = org.springframework.util.StringUtils.commaDelimitedListToSet(context.getPrincipalAttributeNames());

            val principalIdAttributes = new LinkedHashMap<>(attributes);
            if (context.isUseCurrentPrincipalId() && currentPrincipal.isPresent()) {
                val currentPrincipalAttributes = currentPrincipal.get().getAttributes();
                LOGGER.trace("Merging current principal attributes [{}] with resolved attributes [{}]",
                    currentPrincipalAttributes, principalIdAttributes);
                context.getAttributeMerger().mergeAttributes(principalIdAttributes, currentPrincipalAttributes);
            }

            LOGGER.debug("Using principal attributes [{}] to determine principal id", principalIdAttributes);
            val result = attrNames.stream()
                .map(String::trim)
                .filter(principalIdAttributes::containsKey)
                .map(principalIdAttributes::get)
                .findFirst();

            if (result.isEmpty()) {
                LOGGER.warn("Principal resolution is set to resolve users via attribute(s) [{}], and yet "
                        + "the collection of attributes retrieved [{}] do not contain any of those attributes. This is "
                        + "likely due to misconfiguration and CAS will use [{}] as the final principal id",
                    context.getPrincipalAttributeNames(), principalIdAttributes.keySet(), principalId);
                builder.success(false);
            } else {
                val values = result.get();
                if (!values.isEmpty()) {
                    principalId = CollectionUtils.firstElement(values).map(Object::toString).orElseThrow();
                    LOGGER.debug("Found principal id attribute value [{}]", principalId);
                }
            }
        }
        return builder.principalId(principalId).attributes(convertedAttributes).build();
    }

    protected Map<String, List<Object>> retrievePersonAttributes(final String principalId,
                                                                 final Credential credential,
                                                                 final Optional<Principal> currentPrincipal,
                                                                 final Map<String, List<Object>> queryAttributes,
                                                                 final Optional<Service> givenService,
                                                                 final Optional<AuthenticationHandler> handler) throws Throwable {

        queryAttributes.computeIfAbsent("credentialId", __ -> CollectionUtils.wrapList(credential.getId()));
        queryAttributes.computeIfAbsent("credentialClass", __ -> CollectionUtils.wrapList(credential.getClass().getSimpleName()));

        val attributes = new LinkedHashMap<String, List<Object>>();
        currentPrincipal.ifPresent(p -> {
            attributes.putAll(p.getAttributes());
            attributes.put("currentPrincipalId", CollectionUtils.wrap(p.getId()));
        });
        val principal = context.getPrincipalFactory().createPrincipal(principalId, attributes);
        val service = givenService.orElse(null);
        val query = AttributeRepositoryQuery.builder()
            .principal(principal)
            .activeRepositoryIds(context.getActiveAttributeRepositoryIdentifiers())
            .authenticationHandler(handler.orElse(null))
            .service(service)
            .tenant(credential.getTenant())
            .build();

        val repositoryIds = context.getAttributeRepositoryResolver().resolve(query);
        LOGGER.debug("The following attribute repository IDs are resolved: [{}]", repositoryIds);

        val attributeRepository = determineAttributeRepository(query);
        try {
            val attributeFetcher = PrincipalAttributeRepositoryFetcher
                .builder()
                .attributeRepository(attributeRepository)
                .principalId(principalId)
                .activeAttributeRepositoryIdentifiers(repositoryIds)
                .currentPrincipal(currentPrincipal.orElse(null))
                .queryAttributes(queryAttributes)
                .service(service)
                .build();
            return attributeFetcher.retrieve();
        } finally {
            if (attributeRepository instanceof final AggregatingPersonAttributeDao aggregate) {
                val repositories = aggregate.getPersonAttributeDaos();
                for (val repository : repositories) {
                    if (repository.isDisposable() && repository instanceof final DisposableBean db) {
                        db.destroy();
                    }
                }
            }
        }
    }

    private PersonAttributeDao determineAttributeRepository(final AttributeRepositoryQuery query) {
        if (StringUtils.isNotBlank(query.getTenant())) {
            val tenantDefinition = context.getTenantExtractor().getTenantsManager().findTenant(query.getTenant()).orElseThrow();
            if (!tenantDefinition.getProperties().isEmpty()) {
                val builders = context.getApplicationContext().getBeansOfType(TenantPersonAttributeDaoBuilder.class).values();
                val tenantAttributeRepositories = builders
                    .stream()
                    .map(builder -> builder.build(tenantDefinition))
                    .filter(BeanSupplier::isNotProxy)
                    .flatMap(List::stream)
                    .toList();

                if (!tenantAttributeRepositories.isEmpty()) {
                    val mergingAttributeRepository = new MergingPersonAttributeDaoImpl();
                    mergingAttributeRepository.setEnabled(true);
                    mergingAttributeRepository.setPersonAttributeDaos((List) tenantAttributeRepositories);
                    mergingAttributeRepository.setAttributeMerger(context.getAttributeMerger());
                    return mergingAttributeRepository;
                }
            }
        }
        return context.getAttributeRepository();
    }

    /**
     * Extracts the id of the user from the provided credential. This method should be overridden by subclasses to
     * achieve more sophisticated strategies for producing a principal ID from a credential.
     *
     * @param credential       the credential provided by the user.
     * @param currentPrincipal the current principal
     * @return the username, or null if it could not be resolved.
     */
    protected String extractPrincipalId(final Credential credential, final Optional<Principal> currentPrincipal) {
        LOGGER.debug("Extracting credential id based on existing credential [{}]", credential);
        val id = credential.getId();
        if (currentPrincipal.isPresent()) {
            val principal = currentPrincipal.get();
            LOGGER.debug("Principal is currently resolved as [{}]", principal);
            if (context.isUseCurrentPrincipalId()) {
                LOGGER.debug("Using the existing resolved principal id [{}]", principal.getId());
                return principal.getId();
            } else {
                LOGGER.debug("CAS will NOT be using the identifier from the resolved principal [{}] as it's not "
                    + "configured to use the currently-resolved principal id and will fall back onto using the identifier "
                    + "for the credential, that is [{}], for principal resolution", principal, id);
            }
        } else {
            LOGGER.debug("No principal is currently resolved and available. Falling back onto using the identifier "
                + " for the credential, that is [{}], for principal resolution", id);
        }
        LOGGER.debug("Extracted principal id [{}]", id);
        return StringUtils.isNotBlank(id) ? id.trim() : null;
    }

    @SuperBuilder
    @Getter
    static class PrincipalResolutionResult {
        private final String principalId;

        @Builder.Default
        private final Map<String, List<Object>> attributes = new HashMap<>();

        @Builder.Default
        private final boolean success = true;
    }

    /**
     * New person directory principal resolver.
     *
     * @param applicationContext       the application context
     * @param principalFactory         the principal factory
     * @param attributeRepository      the attribute repository
     * @param attributeMerger          the attribute merger
     * @param servicesManager          the services manager
     * @param attributeDefinitionStore the attribute definition store
     * @param personDirectory          the person directory
     * @return the principal resolver
     */
    public static PrincipalResolver newPersonDirectoryPrincipalResolver(
        final ConfigurableApplicationContext applicationContext,
        final PrincipalFactory principalFactory,
        final PersonAttributeDao attributeRepository,
        final AttributeMerger attributeMerger,
        final ServicesManager servicesManager,
        final AttributeDefinitionStore attributeDefinitionStore,
        final AttributeRepositoryResolver attributeRepositoryResolver,
        final PersonDirectoryPrincipalResolverProperties... personDirectory) {
        return newPersonDirectoryPrincipalResolver(applicationContext, principalFactory, attributeRepository,
            attributeMerger, PersonDirectoryPrincipalResolver.class, servicesManager,
            attributeDefinitionStore, attributeRepositoryResolver, personDirectory);
    }

    /**
     * New person directory principal resolver.
     *
     * @param <T>                      the type parameter
     * @param applicationContext       the application context
     * @param principalFactory         the principal factory
     * @param attributeRepository      the attribute repository
     * @param attributeMerger          the attribute merger
     * @param resolverClass            the resolver class
     * @param servicesManager          the services manager
     * @param attributeDefinitionStore the attribute definition store
     * @param personDirectory          the person directory
     * @return the resolver
     */
    public static <T extends PrincipalResolver> T newPersonDirectoryPrincipalResolver(
        final ConfigurableApplicationContext applicationContext,
        final PrincipalFactory principalFactory,
        final PersonAttributeDao attributeRepository,
        final AttributeMerger attributeMerger,
        final Class<T> resolverClass,
        final ServicesManager servicesManager,
        final AttributeDefinitionStore attributeDefinitionStore,
        final AttributeRepositoryResolver attributeRepositoryResolver,
        final PersonDirectoryPrincipalResolverProperties... personDirectory) {

        val context = buildPrincipalResolutionContext(applicationContext, principalFactory, attributeRepository, attributeMerger,
            servicesManager, attributeDefinitionStore, attributeRepositoryResolver, personDirectory);
        return newPersonDirectoryPrincipalResolver(resolverClass, context);
    }

    /**
     * New person directory principal resolver t.
     *
     * @param <T>           the type parameter
     * @param resolverClass the resolver class
     * @param context       the context
     * @return the t
     */
    public static <T extends PrincipalResolver> T newPersonDirectoryPrincipalResolver(final Class<T> resolverClass,
                                                                                      final PrincipalResolutionContext context) {
        return Unchecked.supplier(() -> {
            val ctor = resolverClass.getDeclaredConstructor(PrincipalResolutionContext.class);
            return ctor.newInstance(context);
        }).get();
    }

    /**
     * Build principal resolution context.
     *
     * @param applicationContext          the application context
     * @param principalFactory            the principal factory
     * @param attributeRepository         the attribute repository
     * @param attributeMerger             the attribute merger
     * @param servicesManager             the services manager
     * @param attributeDefinitionStore    the attribute definition store
     * @param attributeRepositoryResolver the attribute repository resolver
     * @param personDirectory             the person directory properties
     * @return the resolver
     */
    public static PrincipalResolutionContext buildPrincipalResolutionContext(
        final ConfigurableApplicationContext applicationContext,
        final PrincipalFactory principalFactory,
        final PersonAttributeDao attributeRepository,
        final AttributeMerger attributeMerger,
        final ServicesManager servicesManager,
        final AttributeDefinitionStore attributeDefinitionStore,
        final AttributeRepositoryResolver attributeRepositoryResolver,
        final PersonDirectoryPrincipalResolverProperties... personDirectory) {

        val transformers = Arrays.stream(personDirectory)
            .map(p -> PrincipalNameTransformerUtils.newPrincipalNameTransformer(p.getPrincipalTransformation()))
            .collect(Collectors.toList());
        val transformer = new ChainingPrincipalNameTransformer(transformers);

        val activeAttributeRepositoryIdentifiers = PrincipalResolverUtils.buildActiveAttributeRepositoryIds(personDirectory);
        return PrincipalResolutionContext.builder()
            .servicesManager(servicesManager)
            .applicationContext(applicationContext)
            .attributeDefinitionStore(attributeDefinitionStore)
            .attributeRepository(attributeRepository)
            .attributeMerger(attributeMerger)
            .principalFactory(principalFactory)
            .returnNullIfNoAttributes(Arrays.stream(personDirectory).filter(p -> p.getReturnNull() != TriStateBoolean.UNDEFINED)
                .map(p -> p.getReturnNull().toBoolean()).findFirst().orElse(Boolean.FALSE))
            .principalAttributeNames(Arrays.stream(personDirectory)
                .map(PersonDirectoryPrincipalResolverProperties::getPrincipalAttribute)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse(StringUtils.EMPTY))
            .principalNameTransformer(transformer)
            .useCurrentPrincipalId(Arrays.stream(personDirectory).filter(p -> p.getUseExistingPrincipalId() != TriStateBoolean.UNDEFINED)
                .map(p -> p.getUseExistingPrincipalId().toBoolean()).findFirst().orElse(Boolean.FALSE))
            .resolveAttributes(Arrays.stream(personDirectory).filter(p -> p.getAttributeResolutionEnabled() != TriStateBoolean.UNDEFINED)
                .map(p -> p.getAttributeResolutionEnabled().toBoolean()).findFirst().orElse(Boolean.TRUE))
            .activeAttributeRepositoryIdentifiers(activeAttributeRepositoryIdentifiers)
            .attributeRepositoryResolver(attributeRepositoryResolver)
            .tenantExtractor(applicationContext.getBean(TenantExtractor.BEAN_NAME, TenantExtractor.class))
            .build();
    }
}
