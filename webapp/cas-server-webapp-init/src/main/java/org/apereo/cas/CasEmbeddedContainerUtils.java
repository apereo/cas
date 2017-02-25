package org.apereo.cas;

import org.apereo.cas.util.spring.boot.AbstractCasBanner;
import org.apereo.cas.util.spring.boot.DefaultCasBanner;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link CasEmbeddedContainerUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public final class CasEmbeddedContainerUtils {
    /**
     * Property to dictate to the environment whether embedded container is running CAS.
     */
    public static final String EMBEDDED_CONTAINER_CONFIG_ACTIVE = "CasEmbeddedContainerConfigurationActive";

    private static final Logger LOGGER = LoggerFactory.getLogger(CasEmbeddedContainerUtils.class);

    private CasEmbeddedContainerUtils() {
    }

    /**
     * Gets runtime properties.
     *
     * @param embeddedContainerActive the embedded container active
     * @return the runtime properties
     */
    public static Map<String, Object> getRuntimeProperties(final Boolean embeddedContainerActive) {
        final Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(EMBEDDED_CONTAINER_CONFIG_ACTIVE, embeddedContainerActive);
        return properties;
    }

    /**
     * Gets cas banner instance.
     *
     * @return the cas banner instance
     */
    public static Banner getCasBannerInstance() {
        final String packageName = CasEmbeddedContainerUtils.class.getPackage().getName();
        final Reflections reflections =
                new Reflections(new ConfigurationBuilder()
                        .filterInputsBy(new FilterBuilder().includePackage(packageName))
                        .setUrls(ClasspathHelper.forPackage(packageName))
                        .setScanners(new SubTypesScanner(true)));

        final Set<Class<? extends AbstractCasBanner>> subTypes = reflections.getSubTypesOf(AbstractCasBanner.class);
        subTypes.remove(DefaultCasBanner.class);
        
        if (subTypes.isEmpty()) {
            return new DefaultCasBanner();
        }
        try {
            final Class<? extends AbstractCasBanner> clz = subTypes.iterator().next();
            LOGGER.debug("Created banner [{}]", clz);
            return clz.newInstance();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new DefaultCasBanner();
    }
}
