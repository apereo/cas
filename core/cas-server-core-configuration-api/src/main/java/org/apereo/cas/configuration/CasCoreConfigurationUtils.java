package org.apereo.cas.configuration;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.Resource;

import java.util.Map;

/**
 * This is {@link CasCoreConfigurationUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@UtilityClass
public final class CasCoreConfigurationUtils {

    /**
     * Load yaml properties map.
     *
     * @param resource the resource
     * @return the map
     */
    public static Map loadYamlProperties(final Resource... resource) {
        val factory = new YamlPropertiesFactoryBean();
        factory.setResolutionMethod(YamlProcessor.ResolutionMethod.OVERRIDE);
        factory.setResources(resource);
        factory.setSingleton(true);
        factory.afterPropertiesSet();
        return factory.getObject();
    }
}
