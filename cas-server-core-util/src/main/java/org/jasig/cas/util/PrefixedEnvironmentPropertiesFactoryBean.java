package org.jasig.cas.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

/**
 * This is {@link PrefixedEnvironmentPropertiesFactoryBean}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PrefixedEnvironmentPropertiesFactoryBean extends PropertiesFactoryBean {
    @Autowired
    private ConfigurableEnvironment environment;

    private String prefix;

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    protected Properties createProperties() throws IOException {
        final Properties filteredProperties = new Properties();
        this.environment.getPropertySources().forEach(propertySource -> {
            if (propertySource instanceof MapPropertySource) {
                final Map<String, Object> map = ((MapPropertySource) propertySource).getSource();
                map.keySet().stream()
                        .filter(p -> p.startsWith(this.prefix))
                        .sorted()
                        .forEach(k -> {
                            final String value = map.get(k).toString();
                            filteredProperties.put(StringUtils.remove(k, this.prefix),
                                    new ArrayList<>(org.springframework.util.StringUtils.commaDelimitedListToSet(value)));
                        });
            }
        });

        return filteredProperties;
    }
}
