package org.apereo.cas.authentication;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import groovy.lang.GroovyClassLoader;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.support.password.DefaultPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.GroovyPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.RejectResultCodePasswordPolicyHandlingStrategy;
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.CollectionUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

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
     * Transform principal attributes list into map map.
     *
     * @param list the list
     * @return the map
     */
    public static Map<String, Object> transformPrincipalAttributesListIntoMap(final List<String> list) {
        final var map = transformPrincipalAttributesListIntoMultiMap(list);
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
        final Multimap<String, Object> multimap = ArrayListMultimap.create();
        if (list.isEmpty()) {
            LOGGER.debug("No principal attributes are defined");
        } else {
            list.forEach(a -> {
                final var attributeName = a.trim();
                if (attributeName.contains(":")) {
                    final var attrCombo = Splitter.on(":").splitToList(attributeName);
                    final var name = attrCombo.get(0).trim();
                    final var value = attrCombo.get(1).trim();
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
                final ResourceLoader loader = new DefaultResourceLoader();
                final var resource = loader.getResource(selectionCriteria);
                if (resource != null) {
                    final var script = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);

                    final var clz = AccessController.doPrivileged((PrivilegedAction<Class<Predicate>>) () -> {
                        final var classLoader = new GroovyClassLoader(Beans.class.getClassLoader(),
                            new CompilerConfiguration(), true);
                        return classLoader.parseClass(script);
                    });
                    return clz.getDeclaredConstructor().newInstance();
                }
            }

            final Class predicateClazz = ClassUtils.getClass(selectionCriteria);
            return (Predicate<org.apereo.cas.authentication.Credential>) predicateClazz.getDeclaredConstructor().newInstance();
        } catch (final Exception e) {
            final var predicate = Pattern.compile(selectionCriteria).asPredicate();
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

        final var location = properties.getGroovy().getLocation();
        if (properties.getStrategy() == PasswordPolicyProperties.PasswordPolicyHandlingOptions.GROOVY && location != null) {
            LOGGER.debug("Created password policy handling strategy based on Groovy script [{}]", location);
            return new GroovyPasswordPolicyHandlingStrategy(location);
        }

        LOGGER.debug("Created default password policy handling strategy");
        return new DefaultPasswordPolicyHandlingStrategy();
    }
}
