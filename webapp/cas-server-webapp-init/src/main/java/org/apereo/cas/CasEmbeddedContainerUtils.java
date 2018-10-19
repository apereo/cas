package org.apereo.cas;

import org.apereo.cas.util.spring.boot.AbstractCasBanner;
import org.apereo.cas.util.spring.boot.DefaultCasBanner;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.springframework.boot.Banner;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link CasEmbeddedContainerUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@UtilityClass
public class CasEmbeddedContainerUtils {
    /**
     * Property to dictate to the environment whether embedded container is running CAS.
     */
    public static final String EMBEDDED_CONTAINER_CONFIG_ACTIVE = "CasEmbeddedContainerConfigurationActive";

    /**
     * Gets runtime properties.
     *
     * @param embeddedContainerActive the embedded container active
     * @return the runtime properties
     */
    public static Map<String, Object> getRuntimeProperties(final Boolean embeddedContainerActive) {
        val properties = new HashMap<String, Object>();
        properties.put(EMBEDDED_CONTAINER_CONFIG_ACTIVE, embeddedContainerActive);
        return properties;
    }

    /**
     * Gets cas banner instance.
     *
     * @return the cas banner instance
     */
    public static Banner getCasBannerInstance() {
        val packageName = CasEmbeddedContainerUtils.class.getPackage().getName();
        val reflections = new Reflections(new ConfigurationBuilder()
                .filterInputsBy(new FilterBuilder().includePackage(packageName))
                .setUrls(ClasspathHelper.forPackage(packageName))
                .setScanners(new SubTypesScanner(true)));

        val subTypes = reflections.getSubTypesOf(AbstractCasBanner.class);
        subTypes.remove(DefaultCasBanner.class);

        if (subTypes.isEmpty()) {
            return new DefaultCasBanner();
        }
        try {
            val clz = subTypes.iterator().next();
            return clz.getDeclaredConstructor().newInstance();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new DefaultCasBanner();
    }
}
