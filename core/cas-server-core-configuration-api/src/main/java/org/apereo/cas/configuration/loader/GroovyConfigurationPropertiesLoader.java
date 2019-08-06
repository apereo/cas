package org.apereo.cas.configuration.loader;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import groovy.util.ConfigSlurper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * This is {@link GroovyConfigurationPropertiesLoader}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class GroovyConfigurationPropertiesLoader extends BaseConfigurationPropertiesLoader {
    private final List<String> applicationProfiles;

    public GroovyConfigurationPropertiesLoader(final CipherExecutor<String, String> configurationCipherExecutor,
                                               final String name,
                                               final List<String> applicationProfiles,
                                               final Resource resource) {
        super(configurationCipherExecutor, name, resource);
        this.applicationProfiles = applicationProfiles;
    }

    @Override
    public PropertySource load() {
        val properties = new LinkedHashMap<Object, Object>();
        val slurper = new ConfigSlurper();
        applicationProfiles.forEach(Unchecked.consumer(profile -> {
            slurper.setEnvironment(profile);
            slurper.registerConditionalBlock("profiles", profile);
            val bindings = CollectionUtils.wrap("profile", profile, "logger", LOGGER);
            slurper.setBinding(bindings);
            val groovyConfig = slurper.parse(getResource().getURL());
            val pp = groovyConfig.toProperties();
            LOGGER.debug("Found settings [{}] in Groovy file [{}]", pp.keySet(), getResource());
            properties.putAll(pp);
        }));
        return finalizeProperties(decryptProperties(properties));
    }
}
