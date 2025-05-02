package org.apereo.cas.services.util;

import org.apereo.cas.services.RegisteredServiceAccessStrategyRequest;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link RegisteredServiceAccessStrategyEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SuperBuilder
@Getter
@Slf4j
public class RegisteredServiceAccessStrategyEvaluator implements Function<RegisteredServiceAccessStrategyRequest, Boolean> {
    /**
     * Collection of required attributes
     * for this service to proceed.
     */
    @Builder.Default
    private Map<String, Set<String>> requiredAttributes = new HashMap<>();

    /**
     * Collection of attributes
     * that will be rejected which will cause this
     * policy to refuse access.
     */
    @Builder.Default
    private Map<String, Set<String>> rejectedAttributes = new HashMap<>();

    /**
     * Indicates whether matching on required attribute values
     * should be done in a case-insensitive manner.
     */
    private boolean caseInsensitive;

    /**
     * Defines the attribute aggregation behavior when checking for required attributes.
     * Default requires that all attributes be present and match the principal's.
     */
    @Builder.Default
    private boolean requireAllAttributes = true;

    @Override
    public Boolean apply(final RegisteredServiceAccessStrategyRequest request) {
        if ((this.rejectedAttributes == null || this.rejectedAttributes.isEmpty())
            && (this.requiredAttributes == null || this.requiredAttributes.isEmpty())) {
            LOGGER.trace("Skipping access strategy policy, since no attributes rules are defined");
            return true;
        }
        if (!enoughAttributesAvailableToProcess(request)) {
            LOGGER.debug("Access is denied. There are not enough attributes available to satisfy requirements");
            return false;
        }
        if (doRejectedAttributesRefusePrincipalAccess(request)) {
            LOGGER.debug("Access is denied. The principal carries attributes that would reject service access");
            return false;
        }
        if (!doRequiredAttributesAllowPrincipalAccess(request, this.requiredAttributes)) {
            LOGGER.debug("Access is denied. The principal does not have the required attributes [{}]", this.requiredAttributes);
            return false;
        }
        return true;
    }

    protected boolean doRequiredAttributesAllowPrincipalAccess(final RegisteredServiceAccessStrategyRequest request,
                                                               final Map<String, Set<String>> requiredAttributes) {
        LOGGER.debug("These required attributes [{}] are examined against [{}] before service can proceed.",
            requiredAttributes, request.getAttributes());
        return requiredAttributes.isEmpty() || requiredAttributesFoundInMap(request, requiredAttributes);
    }

    protected boolean doRejectedAttributesRefusePrincipalAccess(final RegisteredServiceAccessStrategyRequest request) {
        LOGGER.debug("These rejected attributes [{}] are examined against [{}] before service can proceed.",
            rejectedAttributes, request.getAttributes());
        return !rejectedAttributes.isEmpty() && requiredAttributesFoundInMap(request, rejectedAttributes);
    }

    /**
     * Enough attributes available to process? Check collection sizes and determine
     * if we have enough data to move on.
     *
     * @param request the request
     * @return true /false
     */
    protected boolean enoughAttributesAvailableToProcess(final RegisteredServiceAccessStrategyRequest request) {
        if (!enoughRequiredAttributesAvailableToProcess(request.getAttributes(), this.requiredAttributes)) {
            return false;
        }
        if (request.getAttributes().size() < this.rejectedAttributes.size()) {
            LOGGER.debug("The size of the principal attributes that are [{}] does not match defined rejected attributes, "
                         + "which means the principal is not carrying enough data to grant authorization", request.getAttributes());
            return false;
        }
        return true;
    }

    /**
     * Enough required attributes available to process? Check collection sizes and determine
     * if we have enough data to move on.
     *
     * @param principalAttributes the principal attributes
     * @param requiredAttributes  the required attributes
     * @return true /false
     */
    protected boolean enoughRequiredAttributesAvailableToProcess(final Map<String, List<Object>> principalAttributes,
                                                                 final Map<String, Set<String>> requiredAttributes) {
        if (principalAttributes.isEmpty() && !requiredAttributes.isEmpty()) {
            LOGGER.debug("No principal attributes are found to satisfy defined attribute requirements");
            return false;
        }
        if (principalAttributes.size() < requiredAttributes.size()) {
            LOGGER.debug("The size of the principal attributes that are [{}] does not match defined required attributes, "
                         + "which indicates the principal is not carrying enough data to grant authorization", principalAttributes);
            return false;
        }
        return true;
    }

    protected boolean requiredAttributesFoundInMap(final RegisteredServiceAccessStrategyRequest request,
                                                   final Map<String, Set<String>> requiredAttributes) {
        val difference = requiredAttributes.keySet()
            .stream()
            .filter(key -> request.getAttributes().containsKey(key))
            .collect(Collectors.toSet());
        LOGGER.debug("Difference of checking required attributes: [{}]", difference);
        if (this.requireAllAttributes && difference.size() < requiredAttributes.size()) {
            return false;
        }
        val attributeMatcherPredicate = Unchecked.<String>predicate(key -> requiredAttributeFound(key, request, requiredAttributes));
        if (this.requireAllAttributes) {
            return difference.stream().allMatch(attributeMatcherPredicate);
        }
        return difference.stream().anyMatch(attributeMatcherPredicate);
    }

    protected boolean requiredAttributeFound(final String attributeName,
                                             final RegisteredServiceAccessStrategyRequest request,
                                             final Map<String, Set<String>> requiredAttributes) throws Throwable {
        val requiredValues = requiredAttributes.get(attributeName);
        val availableValues = CollectionUtils.toCollection(request.getAttributes().get(attributeName));

        val scriptFactoryInstance = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();
        
        val results = new ArrayList<>();
        for (val requiredValue : requiredValues) {
            if (scriptFactoryInstance.isPresent() && scriptFactoryInstance.get().isInlineScript(requiredValue) && CasRuntimeHintsRegistrar.notInNativeImage()) {
                val script = scriptFactoryInstance.get().getInlineScript(requiredValue).orElseThrow();
                try (val executableScript = scriptFactoryInstance.get().fromScript(script)) {
                    val args = CollectionUtils.<String, Object>wrap(
                        "principalId", request.getPrincipalId(),
                        "currentValues", availableValues,
                        "attributes", request.getAttributes(),
                        "logger", LOGGER);
                    executableScript.setBinding(args);
                    results.add(executableScript.execute(args.values().toArray(), Boolean.class));
                }
            } else {
                val pattern = RegexUtils.createPattern(requiredValue, caseInsensitive ? Pattern.CASE_INSENSITIVE : 0);
                LOGGER.debug("Checking [{}] against [{}] with pattern [{}] for attribute [{}]",
                    requiredValues, availableValues, pattern, attributeName);
                if (pattern.equals(RegexUtils.MATCH_NOTHING_PATTERN)) {
                    results.add(availableValues.stream().anyMatch(requiredValues::contains));
                } else {
                    results.add(availableValues.stream().map(Object::toString).anyMatch(pattern.asPredicate()));
                }
            }
        }
        return results.contains(true);
    }
}
