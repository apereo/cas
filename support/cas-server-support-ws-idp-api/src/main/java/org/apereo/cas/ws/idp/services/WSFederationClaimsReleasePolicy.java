package org.apereo.cas.ws.idp.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledGroovyScript;
import org.apereo.cas.util.scripting.GroovyShellScript;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.ws.idp.WSFederationClaims;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.persistence.PostLoad;
import javax.persistence.Transient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is {@link WSFederationClaimsReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class WSFederationClaimsReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
    private static final int MAP_SIZE = 8;
    private static final long serialVersionUID = -2814928645221579489L;

    private Map<String, String> allowedAttributes = new LinkedHashMap<>(MAP_SIZE);

    @JsonIgnore
    @Transient
    @org.springframework.data.annotation.Transient
    private transient Map<String, ExecutableCompiledGroovyScript> attributeScriptCache = new LinkedHashMap<>(MAP_SIZE);

    public WSFederationClaimsReleasePolicy() {
        this.allowedAttributes = new LinkedHashMap<>(MAP_SIZE);
    }

    public WSFederationClaimsReleasePolicy(final Map<String, String> allowedAttributes) {
        setAllowedAttributes(allowedAttributes);
    }

    private static void mapSimpleSingleAttributeDefinition(final String attributeName,
                                                           final String mappedAttributeName,
                                                           final List<Object> attributeValue,
                                                           final Map<String, List<Object>> attributesToRelease) {
        if (attributeValue != null) {
            LOGGER.debug("Found attribute [{}] in the list of allowed attributes, mapped to the name [{}]",
                attributeName, mappedAttributeName);
            val values = CollectionUtils.toCollection(attributeValue, ArrayList.class);
            attributesToRelease.put(attributeName, values);
        } else {
            LOGGER.warn("Could not find value for mapped attribute [{}] that is based off of [{}] in the allowed attributes list. "
                    + "Ensure the original attribute [{}] is retrieved and contains at least a single value. Attribute [{}] "
                    + "will and can not be released without the presence of a value.", mappedAttributeName, attributeName,
                attributeName, mappedAttributeName);
        }
    }

    @Override
    public Map<String, List<Object>> getAttributesInternal(final Principal principal, final Map<String, List<Object>> attrs,
                                                           final RegisteredService registeredService, final Service selectedService) {
        initializeWatchableScriptIfNeeded();

        val resolvedAttributes = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attrs);
        val attributesToRelease = Maps.<String, List<Object>>newHashMapWithExpectedSize(resolvedAttributes.size());

        getAllowedAttributes().entrySet()
            .stream()
            .filter(entry -> WSFederationClaims.contains(entry.getKey().toUpperCase()))
            .forEach(entry -> {
                val claimName = entry.getKey();
                val attributeValue = resolvedAttributes.get(entry.getValue());
                val claim = WSFederationClaims.valueOf(claimName.toUpperCase());
                LOGGER.trace("Evaluating claim [{}] mapped to attribute value [{}]", claim.getUri(), attributeValue);
                mapSingleAttributeDefinition(claim.getUri(), entry.getValue(), attributeValue, resolvedAttributes, attributesToRelease);
            });
        return attributesToRelease;
    }

    @PostLoad
    private void initializeWatchableScriptIfNeeded() {
        if (this.attributeScriptCache == null) {
            this.attributeScriptCache = new LinkedHashMap<>(MAP_SIZE);
        }
        getAllowedAttributes()
            .entrySet()
            .stream()
            .filter(entry -> WSFederationClaims.contains(entry.getKey()))
            .forEach(entry -> {
                if (!attributeScriptCache.containsKey(entry.getValue())) {
                    val matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(entry.getValue());
                    val matcherFile = ScriptingUtils.getMatcherForExternalGroovyScript(entry.getValue());
                    val claim = WSFederationClaims.valueOf(entry.getKey().toUpperCase());
                    if (matcherInline.find()) {
                        attributeScriptCache.put(claim.getUri(), new GroovyShellScript(matcherInline.group(1)));
                    } else if (matcherFile.find()) {
                        try {
                            val scriptPath = SpringExpressionLanguageValueResolver.getInstance().resolve(matcherFile.group());
                            val resource = ResourceUtils.getRawResourceFrom(scriptPath);
                            attributeScriptCache.put(claim.getUri(), new WatchableGroovyScriptResource(resource));
                        } catch (final Exception e) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.error(e.getMessage(), e);
                            } else {
                                LOGGER.error(e.getMessage());
                            }
                        }
                    }
                }
            });
    }

    private void mapSingleAttributeDefinition(final String attributeName,
                                              final String mappedAttributeName,
                                              final List<Object> attributeValue,
                                              final Map<String, List<Object>> resolvedAttributes,
                                              final Map<String, List<Object>> attributesToRelease) {
        if (attributeScriptCache.containsKey(attributeName)) {
            val script = attributeScriptCache.get(attributeName);
            val args = CollectionUtils.wrap("attributes", resolvedAttributes, "logger", LOGGER);
            script.setBinding(args);
            val result = script.execute(args.values().toArray(), Object.class);
            if (result != null) {
                LOGGER.debug("Mapped attribute [{}] to [{}] from script", attributeName, result);
                attributesToRelease.put(attributeName, CollectionUtils.wrapList(result));
            } else {
                LOGGER.warn("Groovy-scripted attribute returned no value for [{}]", attributeName);
            }
        } else {
            mapSimpleSingleAttributeDefinition(attributeName, mappedAttributeName, attributeValue, attributesToRelease);
        }
    }
}
