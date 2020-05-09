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
