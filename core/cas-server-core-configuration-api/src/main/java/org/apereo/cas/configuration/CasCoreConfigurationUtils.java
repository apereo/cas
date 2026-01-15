package org.apereo.cas.configuration;

import module java.base;
import org.apereo.cas.configuration.api.MutablePropertySource;
import com.google.common.base.Splitter;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.cloud.bootstrap.config.BootstrapPropertySource;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;

/**
 * This is {@link CasCoreConfigurationUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@UtilityClass
public final class CasCoreConfigurationUtils {
    private static final Pattern INDEXED = Pattern.compile("(.+)\\[(\\d+)]");

    /**
     * Load yaml properties map.
     *
     * @param resource the resource
     * @return the map
     */
    public static Map<String, Object> loadYamlProperties(final Resource... resource) {
        val factory = new YamlPropertiesFactoryBean();
        factory.setResolutionMethod(YamlProcessor.ResolutionMethod.OVERRIDE);
        factory.setResources(resource);
        factory.setSingleton(true);
        factory.afterPropertiesSet();
        return (Map) Objects.requireNonNull(factory.getObject());
    }

    /**
     * Gets mutable property sources.
     *
     * @param applicationContext the application context
     * @return the mutable property sources
     */
    @SuppressWarnings("NullAway")
    public static List<MutablePropertySource> getMutablePropertySources(
        final ConfigurableApplicationContext applicationContext) {
        val activeSources = applicationContext.getEnvironment().getPropertySources();
        return activeSources
            .stream()
            .map(source -> {
                if (source instanceof final MutablePropertySource mutable) {
                    return mutable;
                }
                if (source instanceof final BootstrapPropertySource bootstrap
                    && bootstrap.getDelegate() instanceof final MutablePropertySource mutable) {
                    return mutable;
                }
                return null;
            })
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Convert to map.
     *
     * @param flatProperties the properties
     * @return the map
     */
    public static Map<String, Object> convertToNestedMap(final Map<String, Object> flatProperties) {
        val root = new LinkedHashMap<String, Object>();
        for (val entry : flatProperties.entrySet()) {
            putKey(root, entry.getKey(), entry.getValue());
        }
        return root;
    }

    private static void putKey(
        final Map<String, Object> root,
        final String dottedKey,
        final Object value) {
        val parts = Splitter.on('.').splitToList(dottedKey);
        var current = root;

        for (var i = 0; i < parts.size(); i++) {
            val last = i == parts.size() - 1;
            val part = parts.get(i);

            val matcher = INDEXED.matcher(part);

            if (matcher.matches()) {
                val name = matcher.group(1);
                val index = Integer.parseInt(matcher.group(2));
                val list = (List<Object>) current.computeIfAbsent(name, _ -> new ArrayList<>());

                while (list.size() < index + 1) {
                    list.add(null);
                }

                if (last) {
                    list.set(index, value);
                } else {
                    val next = list.get(index);
                    if (next == null) {
                        val map = new LinkedHashMap<String, Object>();
                        list.set(index, map);
                        current = map;
                    } else if (next instanceof final Map nextMap) {
                        current = nextMap;
                    }
                }
            } else {
                if (last) {
                    current.put(part, value);
                } else {
                    current = (Map<String, Object>) current.computeIfAbsent(part, _ -> new LinkedHashMap<>());
                }
            }
        }
    }
}
