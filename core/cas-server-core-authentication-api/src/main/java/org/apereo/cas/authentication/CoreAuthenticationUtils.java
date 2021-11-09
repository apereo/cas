package org.apereo.cas.authentication;

import org.apereo.cas.authentication.adaptive.intel.DefaultIPAddressIntelligenceService;
import org.apereo.cas.authentication.adaptive.intel.GroovyIPAddressIntelligenceService;
import org.apereo.cas.authentication.adaptive.intel.IPAddressIntelligenceService;
import org.apereo.cas.authentication.adaptive.intel.RestfulIPAddressIntelligenceService;
import org.apereo.cas.authentication.policy.AllAuthenticationHandlersSucceededAuthenticationPolicy;
import org.apereo.cas.authentication.policy.AllCredentialsValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.AtLeastOneCredentialValidatedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.GroovyScriptAuthenticationPolicy;
import org.apereo.cas.authentication.policy.NotPreventedAuthenticationPolicy;
import org.apereo.cas.authentication.policy.RequiredAuthenticationHandlerAuthenticationPolicy;
import org.apereo.cas.authentication.policy.RestfulAuthenticationPolicy;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;
import org.apereo.cas.authentication.support.password.DefaultPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.GroovyPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.RejectResultCodePasswordPolicyHandlingStrategy;
import org.apereo.cas.configuration.model.core.authentication.AdaptiveAuthenticationProperties;
import org.apereo.cas.configuration.model.core.authentication.AuthenticationPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.apereo.cas.configuration.model.core.authentication.PersonDirectoryPrincipalResolverProperties;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.model.TriStateBoolean;
import org.apereo.cas.validation.Assertion;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import groovy.lang.GroovyClassLoader;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.support.merger.BaseAdditiveAttributeMerger;
import org.apereo.services.persondir.support.merger.IAttributeMerger;
import org.apereo.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.apereo.services.persondir.support.merger.NoncollidingAttributeAdder;
import org.apereo.services.persondir.support.merger.ReplacingAttributeAdder;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link CoreAuthenticationUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */

@Slf4j
@UtilityClass
public class CoreAuthenticationUtils {

    /**
     * Convert attribute values to multi valued objects.
     *
     * @param attributes the attributes
     * @return the map of attributes to return
     */
    public static Map<String, List<Object>> convertAttributeValuesToMultiValuedObjects(final Map<String, Object> attributes) {
        val entries = attributes.entrySet();
        return entries
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                val value = entry.getValue();
                return CollectionUtils.toCollection(value, ArrayList.class);
            }));
    }

    /**
     * Retrieve attributes from attribute repository and return map.
     *
     * @param attributeRepository                  the attribute repository
     * @param principalId                          the principal id
     * @param activeAttributeRepositoryIdentifiers the active attribute repository identifiers
     * @param currentPrincipal                     the current principal
     * @return the map or null
     */
    public static Map<String, List<Object>> retrieveAttributesFromAttributeRepository(final IPersonAttributeDao attributeRepository,
                                                                                      final String principalId,
                                                                                      final Set<String> activeAttributeRepositoryIdentifiers,
                                                                                      final Optional<Principal> currentPrincipal) {
        var filter = IPersonAttributeDaoFilter.alwaysChoose();
        if (activeAttributeRepositoryIdentifiers != null && !activeAttributeRepositoryIdentifiers.isEmpty()) {
            val repoIdsArray = activeAttributeRepositoryIdentifiers.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
            filter = dao -> Arrays.stream(dao.getId())
                .anyMatch(daoId -> daoId.equalsIgnoreCase(IPersonAttributeDao.WILDCARD)
                    || StringUtils.equalsAnyIgnoreCase(daoId, repoIdsArray)
                    || StringUtils.equalsAnyIgnoreCase(IPersonAttributeDao.WILDCARD, repoIdsArray));
        }

        val attrs = attributeRepository.getPerson(principalId, filter);
        if (attrs == null) {
            return new HashMap<>(0);
        }
        return attrs.getAttributes();
    }

    /**
     * Gets attribute merger.
     *
     * @param mergingPolicy the merging policy
     * @return the attribute merger
     */
    public static IAttributeMerger getAttributeMerger(final PrincipalAttributesCoreProperties.MergingStrategyTypes mergingPolicy) {
        switch (mergingPolicy) {
            case MULTIVALUED:
                val merger = new MultivaluedAttributeMerger();
                merger.setDistinctValues(true);
                return merger;
            case ADD:
                return new NoncollidingAttributeAdder();
            case NONE:
                return new BaseAdditiveAttributeMerger() {
                    @Override
                    protected Map<String, List<Object>> mergePersonAttributes(final Map<String, List<Object>> toModify,
                                                                              final Map<String, List<Object>> toConsider) {
                        return new LinkedHashMap<>(toModify);
                    }
                };
            case REPLACE:
            default:
                return new ReplacingAttributeAdder();
        }
    }

    /**
     * Is remember me authentication?
     * looks at the authentication object to find {@link RememberMeCredential#AUTHENTICATION_ATTRIBUTE_REMEMBER_ME}
     * and expects the assertion to also note a new login session.
     *
     * @param model     the model
     * @param assertion the assertion
     * @return true if remember-me, false if otherwise.
     */
    public static boolean isRememberMeAuthentication(final Authentication model, final Assertion assertion) {
        val authnAttributes = model.getAttributes();
        val authnMethod = authnAttributes.get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);
        return authnMethod != null && authnMethod.contains(Boolean.TRUE) && assertion.isFromNewLogin();
    }

    /**
     * Is remember me recorded in authentication.
     *
     * @param authentication the authentication
     * @return the boolean
     */
    public static Boolean isRememberMeAuthentication(final Authentication authentication) {
        if (authentication == null) {
            return Boolean.FALSE;
        }
        val attributes = authentication.getAttributes();
        LOGGER.trace("Located authentication attributes [{}]", attributes);

        if (attributes.containsKey(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME)) {
            val rememberMeValue = attributes.get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);
            LOGGER.debug("Located remember-me authentication attribute [{}]", rememberMeValue);
            return rememberMeValue.contains(Boolean.TRUE);
        }
        return Boolean.FALSE;
    }

    /**
     * Merge attributes map.
     *
     * @param currentAttributes the current attributes
     * @param attributesToMerge the attributes to merge
     * @param merger            the merger
     * @return the map
     */
    public static Map<String, List<Object>> mergeAttributes(final Map<String, List<Object>> currentAttributes,
                                                            final Map<String, List<Object>> attributesToMerge,
                                                            final IAttributeMerger merger) {
        val toModify = currentAttributes.entrySet()
            .stream()
            .map(entry -> Pair.of(entry.getKey(), CollectionUtils.toCollection(entry.getValue(), ArrayList.class)))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        val toMerge = attributesToMerge.entrySet()
            .stream()
            .map(entry -> Pair.of(entry.getKey(), CollectionUtils.toCollection(entry.getValue(), ArrayList.class)))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        LOGGER.trace("Merging current attributes [{}] with [{}]", toModify, toMerge);
        val results = merger.mergeAttributes((Map) toModify, (Map) toMerge);
        LOGGER.debug("Merged attributes with the final result as [{}]", results);
        return results;
    }

    /**
     * Merge attributes map.
     *
     * @param currentAttributes the current attributes
     * @param attributesToMerge the attributes to merge
     * @return the map
     */
    public static Map<String, List<Object>> mergeAttributes(final Map<String, List<Object>> currentAttributes,
                                                            final Map<String, List<Object>> attributesToMerge) {
        val merger = new MultivaluedAttributeMerger();
        merger.setDistinctValues(true);
        return mergeAttributes(currentAttributes, attributesToMerge, merger);
    }

    /**
     * Transform principal attributes list into map map.
     *
     * @param list the list
     * @return the map
     */
    public static Map<String, Object> transformPrincipalAttributesListIntoMap(final List<String> list) {
        val map = transformPrincipalAttributesListIntoMultiMap(list);
        return CollectionUtils.wrap(map);
    }

    /**
     * Transform principal attributes into map.
     * Items in the list are defined in the syntax of "cn", or "cn:commonName" for virtual renaming and maps.
     *
     * @param list the list
     * @return the map
     */
    public static Multimap<String, Object> transformPrincipalAttributesListIntoMultiMap(final List<String> list) {
        val multimap = ArrayListMultimap.<String, Object>create();
        if (list.isEmpty()) {
            LOGGER.debug("No principal attributes are defined");
        } else {
            list.forEach(a -> {
                val attributeName = a.trim();
                if (attributeName.contains(":")) {
                    val attrCombo = Splitter.on(":").splitToList(attributeName);
                    val name = attrCombo.get(0).trim();
                    val value = attrCombo.get(1).trim();
                    LOGGER.debug("Mapped principal attribute name [{}] to [{}]", name, value);
                    multimap.put(name, value);
                } else {
                    LOGGER.debug("Mapped principal attribute name [{}]", attributeName);
                    multimap.put(attributeName, attributeName);
                }
            });
        }
        return multimap;
    }


    /**
     * Gets credential selection predicate.
     *
     * @param selectionCriteria the selection criteria
     * @return the credential selection predicate
     */
    public static Predicate<Credential> newCredentialSelectionPredicate(final String selectionCriteria) {
        try {
            if (StringUtils.isBlank(selectionCriteria)) {
                return credential -> true;
            }
            if (selectionCriteria.endsWith(".groovy")) {
                val loader = new DefaultResourceLoader();
                val resource = loader.getResource(selectionCriteria);
                val script = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
                val classLoader = new GroovyClassLoader(Beans.class.getClassLoader(),
                    new CompilerConfiguration(), true);
                val clz = classLoader.parseClass(script);
                return (Predicate<Credential>) clz.getDeclaredConstructor().newInstance();
            }
            val predicateClazz = ClassUtils.getClass(selectionCriteria);
            return (Predicate<Credential>) predicateClazz.getDeclaredConstructor().newInstance();
        } catch (final Exception e) {
            val predicate = Pattern.compile(selectionCriteria).asPredicate();
            return credential -> predicate.test(credential.getId());
        }
    }

    /**
     * New password policy handling strategy.
     *
     * @param properties         the properties
     * @param applicationContext the application context
     * @return the authentication password policy handling strategy
     */
    public static AuthenticationPasswordPolicyHandlingStrategy newPasswordPolicyHandlingStrategy(final PasswordPolicyProperties properties,
                                                                                                 final ApplicationContext applicationContext) {
        if (properties.getStrategy() == PasswordPolicyProperties.PasswordPolicyHandlingOptions.REJECT_RESULT_CODE) {
            LOGGER.debug("Created password policy handling strategy based on blocked authentication result codes");
            return new RejectResultCodePasswordPolicyHandlingStrategy<>();
        }

        val location = properties.getGroovy().getLocation();
        if (properties.getStrategy() == PasswordPolicyProperties.PasswordPolicyHandlingOptions.GROOVY && location != null) {
            LOGGER.debug("Created password policy handling strategy based on Groovy script [{}]", location);
            return new GroovyPasswordPolicyHandlingStrategy<>(location, applicationContext);
        }

        LOGGER.trace("Created default password policy handling strategy");
        return new DefaultPasswordPolicyHandlingStrategy<>();
    }

    /**
     * New person directory principal resolver.
     *
     * @param principalFactory    the principal factory
     * @param attributeRepository the attribute repository
     * @param attributeMerger     the attribute merger
     * @param personDirectory     the person directory
     * @return the principal resolver
     */
    public static PrincipalResolver newPersonDirectoryPrincipalResolver(
        final PrincipalFactory principalFactory,
        final IPersonAttributeDao attributeRepository,
        final IAttributeMerger attributeMerger,
        final PersonDirectoryPrincipalResolverProperties... personDirectory) {
        return newPersonDirectoryPrincipalResolver(principalFactory, attributeRepository,
            attributeMerger, PersonDirectoryPrincipalResolver.class, personDirectory);
    }

    /**
     * New person directory principal resolver.
     *
     * @param <T>                 the type parameter
     * @param principalFactory    the principal factory
     * @param attributeRepository the attribute repository
     * @param attributeMerger     the attribute merger
     * @param resolverClass       the resolver class
     * @param personDirectory     the person directory
     * @return the resolver
     */
    @SneakyThrows
    public static <T extends PrincipalResolver> T newPersonDirectoryPrincipalResolver(
        final PrincipalFactory principalFactory,
        final IPersonAttributeDao attributeRepository,
        final IAttributeMerger attributeMerger,
        final Class<T> resolverClass,
        final PersonDirectoryPrincipalResolverProperties... personDirectory) {
        val context = buildPrincipalResolutionContext(principalFactory, attributeRepository, attributeMerger, personDirectory);

        val ctor = resolverClass.getDeclaredConstructor(PrincipalResolutionContext.class);
        return ctor.newInstance(context);
    }

    /**
     * New PrincipalResolutionContext.
     *
     * @param principalFactory    the principal factory
     * @param attributeRepository the attribute repository
     * @param attributeMerger     the attribute merger
     * @param personDirectory     the person directory properties
     * @return the resolver
     */
    public static PrincipalResolutionContext buildPrincipalResolutionContext(final PrincipalFactory principalFactory,
                                                                             final IPersonAttributeDao attributeRepository,
                                                                             final IAttributeMerger attributeMerger,
                                                                             final PersonDirectoryPrincipalResolverProperties... personDirectory) {
        return PrincipalResolutionContext.builder()
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
            .principalNameTransformer(formUserId -> formUserId)
            .useCurrentPrincipalId(Arrays.stream(personDirectory).filter(p -> p.getUseExistingPrincipalId() != TriStateBoolean.UNDEFINED)
                .map(p -> p.getUseExistingPrincipalId().toBoolean()).findFirst().orElse(Boolean.FALSE))
            .resolveAttributes(Arrays.stream(personDirectory).filter(p -> p.getAttributeResolutionEnabled() != TriStateBoolean.UNDEFINED)
                .map(p -> p.getAttributeResolutionEnabled().toBoolean()).findFirst().orElse(Boolean.TRUE))
            .activeAttributeRepositoryIdentifiers(Arrays.stream(personDirectory)
                .filter(p -> StringUtils.isNotBlank(p.getActiveAttributeRepositoryIds()))
                .map(p -> org.springframework.util.StringUtils.commaDelimitedListToSet(p.getActiveAttributeRepositoryIds()))
                .filter(p -> !p.isEmpty())
                .findFirst()
                .orElse(Collections.EMPTY_SET))
            .build();
    }

    /**
     * New authentication policy collection.
     *
     * @param policyProps the policy props
     * @return the collection
     */
    public static Collection<AuthenticationPolicy> newAuthenticationPolicy(final AuthenticationPolicyProperties policyProps) {
        if (policyProps.getReq().isEnabled()) {
            LOGGER.trace("Activating authentication policy [{}]", RequiredAuthenticationHandlerAuthenticationPolicy.class.getSimpleName());
            val requiredHandlerNames = org.springframework.util.StringUtils.commaDelimitedListToSet(policyProps.getReq().getHandlerName());
            var policy = new RequiredAuthenticationHandlerAuthenticationPolicy(requiredHandlerNames, policyProps.getReq().isTryAll());
            return CollectionUtils.wrapList(policy);
        }

        if (policyProps.getAllHandlers().isEnabled()) {
            LOGGER.trace("Activating authentication policy [{}]", AllAuthenticationHandlersSucceededAuthenticationPolicy.class.getSimpleName());
            return CollectionUtils.wrapList(new AllAuthenticationHandlersSucceededAuthenticationPolicy());
        }

        if (policyProps.getAll().isEnabled()) {
            LOGGER.trace("Activating authentication policy [{}]", AllCredentialsValidatedAuthenticationPolicy.class.getSimpleName());
            return CollectionUtils.wrapList(new AllCredentialsValidatedAuthenticationPolicy());
        }

        if (policyProps.getNotPrevented().isEnabled()) {
            LOGGER.trace("Activating authentication policy [{}]", NotPreventedAuthenticationPolicy.class.getSimpleName());
            return CollectionUtils.wrapList(new NotPreventedAuthenticationPolicy());
        }

        if (!policyProps.getGroovy().isEmpty()) {
            LOGGER.trace("Activating authentication policy [{}]", GroovyScriptAuthenticationPolicy.class.getSimpleName());
            return policyProps.getGroovy()
                .stream()
                .map(groovy -> new GroovyScriptAuthenticationPolicy(groovy.getScript()))
                .collect(Collectors.toList());
        }

        if (!policyProps.getRest().isEmpty()) {
            LOGGER.trace("Activating authentication policy [{}]", RestfulAuthenticationPolicy.class.getSimpleName());
            return policyProps.getRest()
                .stream()
                .map(RestfulAuthenticationPolicy::new)
                .collect(Collectors.toList());
        }

        if (policyProps.getAny().isEnabled()) {
            LOGGER.trace("Activating authentication policy [{}]", AtLeastOneCredentialValidatedAuthenticationPolicy.class.getSimpleName());
            return CollectionUtils.wrapList(new AtLeastOneCredentialValidatedAuthenticationPolicy(policyProps.getAny().isTryAll()));
        }
        return new ArrayList<>();
    }

    /**
     * New ip address intelligence service.
     *
     * @param adaptive the adaptive
     * @return the ip address intelligence service
     */
    public static IPAddressIntelligenceService newIpAddressIntelligenceService(final AdaptiveAuthenticationProperties adaptive) {
        val intel = adaptive.getIpIntel();

        if (StringUtils.isNotBlank(intel.getRest().getUrl())) {
            return new RestfulIPAddressIntelligenceService(adaptive);
        }
        if (intel.getGroovy().getLocation() != null) {
            return new GroovyIPAddressIntelligenceService(adaptive);
        }
        if (StringUtils.isNotBlank(intel.getBlackDot().getEmailAddress())) {
            return new RestfulIPAddressIntelligenceService(adaptive);
        }
        return new DefaultIPAddressIntelligenceService(adaptive);
    }

    /**
     * New principal election strategy conflict resolver.
     *
     * @param properties the properties
     * @return the principal election strategy conflict resolver
     */
    public static PrincipalElectionStrategyConflictResolver newPrincipalElectionStrategyConflictResolver(
        final PersonDirectoryPrincipalResolverProperties properties) {
        if (StringUtils.equalsIgnoreCase(properties.getPrincipalResolutionConflictStrategy(), "first")) {
            return PrincipalElectionStrategyConflictResolver.first();
        }
        return PrincipalElectionStrategyConflictResolver.last();
    }


}
