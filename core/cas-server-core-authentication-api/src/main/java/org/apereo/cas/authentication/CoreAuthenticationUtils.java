package org.apereo.cas.authentication;

import org.apereo.cas.authentication.support.password.DefaultPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.GroovyPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.RejectResultCodePasswordPolicyHandlingStrategy;
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.Assertion;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import groovy.lang.GroovyClassLoader;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.services.persondir.support.merger.MultivaluedAttributeMerger;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.core.io.DefaultResourceLoader;

import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
     * Is remember me authentication?
     * looks at the authentication object to find {@link RememberMeCredential#AUTHENTICATION_ATTRIBUTE_REMEMBER_ME}
     * and expects the assertion to also note a new login session.
     *
     * @param model     the model
     * @param assertion the assertion
     * @return true if remember-me, false if otherwise.
     */
    public static boolean isRememberMeAuthentication(final Authentication model, final Assertion assertion) {
        val authnAttributes = convertAttributeValuesToMultiValuedObjects(model.getAttributes());
        val authnMethod = (Collection) authnAttributes.get(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);
        return authnMethod != null && authnMethod.contains(Boolean.TRUE) && assertion.isFromNewLogin();
    }

    /**
     * Convert attribute values to multi valued objects.
     *
     * @param attributes the attributes
     * @return the map of attributes to return
     */
    public static Map<String, Object> convertAttributeValuesToMultiValuedObjects(final Map<String, Object> attributes) {
        val entries = attributes.entrySet();
        return entries.stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                val value = entry.getValue();
                if (value instanceof Collection || value instanceof Map || value instanceof Object[] || value instanceof Iterator || value instanceof Enumeration) {
                    return value;
                }
                return CollectionUtils.wrap(value);
            }));
    }

    /**
     * Merge attributes map.
     *
     * @param currentAttributes the current attributes
     * @param attributesToMerge the attributes to merge
     * @return the map
     */
    public static Map<String, Object> mergeAttributes(final Map<String, Object> currentAttributes, final Map<String, Object> attributesToMerge) {
        val merger = new MultivaluedAttributeMerger();

        val toModify = currentAttributes.entrySet()
            .stream()
            .map(entry -> Pair.of(entry.getKey(), CollectionUtils.toCollection(entry.getValue(), ArrayList.class)))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        val toMerge = attributesToMerge.entrySet()
            .stream()
            .map(entry -> Pair.of(entry.getKey(), CollectionUtils.toCollection(entry.getValue(), ArrayList.class)))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

        LOGGER.debug("Merging current attributes [{}] with [{}]", currentAttributes, attributesToMerge);
        val results = merger.mergeAttributes((Map) toModify, (Map) toMerge);
        LOGGER.debug("Merged attributes with the final result as [{}]", results);
        return results;
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
                if (resource != null) {
                    val script = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);

                    val clz = AccessController.doPrivileged((PrivilegedAction<Class<Predicate>>) () -> {
                        val classLoader = new GroovyClassLoader(Beans.class.getClassLoader(),
                            new CompilerConfiguration(), true);
                        return classLoader.parseClass(script);
                    });
                    return clz.getDeclaredConstructor().newInstance();
                }
            }

            val predicateClazz = ClassUtils.getClass(selectionCriteria);
            return (Predicate<org.apereo.cas.authentication.Credential>) predicateClazz.getDeclaredConstructor().newInstance();
        } catch (final Exception e) {
            val predicate = Pattern.compile(selectionCriteria).asPredicate();
            return credential -> predicate.test(credential.getId());
        }
    }

    /**
     * New password policy handling strategy.
     *
     * @param properties the properties
     * @return the authentication password policy handling strategy
     */
    public static AuthenticationPasswordPolicyHandlingStrategy newPasswordPolicyHandlingStrategy(final PasswordPolicyProperties properties) {
        if (properties.getStrategy() == PasswordPolicyProperties.PasswordPolicyHandlingOptions.REJECT_RESULT_CODE) {
            LOGGER.debug("Created password policy handling strategy based on blacklisted authentication result codes");
            return new RejectResultCodePasswordPolicyHandlingStrategy();
        }

        val location = properties.getGroovy().getLocation();
        if (properties.getStrategy() == PasswordPolicyProperties.PasswordPolicyHandlingOptions.GROOVY && location != null) {
            LOGGER.debug("Created password policy handling strategy based on Groovy script [{}]", location);
            return new GroovyPasswordPolicyHandlingStrategy(location);
        }

        LOGGER.debug("Created default password policy handling strategy");
        return new DefaultPasswordPolicyHandlingStrategy();
    }
}
