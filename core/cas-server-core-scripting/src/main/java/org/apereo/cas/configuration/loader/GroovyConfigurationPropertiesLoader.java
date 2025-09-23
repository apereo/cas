package org.apereo.cas.configuration.loader;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.scripting.ScriptingUtils;
import groovy.util.ConfigSlurper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * This is {@link GroovyConfigurationPropertiesLoader}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@NoArgsConstructor
public class GroovyConfigurationPropertiesLoader extends BaseConfigurationPropertiesLoader {
    
    @Override
    public PropertySource load(final Resource resource,
                               final Environment environment,
                               final String name,
                               final CipherExecutor<String, String> configurationCipherExecutor) {
        val properties = new LinkedHashMap<>();
        val configSlurper = new ConfigSlurper();
        configSlurper.setClassLoader(ScriptingUtils.newGroovyClassLoader());

        val applicationProfiles = getApplicationProfiles(environment);
        applicationProfiles.forEach(Unchecked.consumer(profile -> {
            configSlurper.setEnvironment(profile);
            configSlurper.registerConditionalBlock("profiles", profile);
            val bindings = CollectionUtils.wrap(
                "profile", profile,
                "logger", LOGGER,
                "env", System.getenv()
            );
            configSlurper.setBinding(bindings);
            val groovyConfig = configSlurper.parse(resource.getURL());
            val pp = groovyConfig.toProperties();
            LOGGER.debug("Found settings [{}] in Groovy file [{}]", pp.keySet(), resource);
            properties.putAll(pp);
        }));
        return finalizeProperties(name, decryptProperties(configurationCipherExecutor, properties));
    }

    @Override
    public boolean supports(final Resource resource) {
        val filename = StringUtils.defaultString(resource.getFilename()).toLowerCase(Locale.ENGLISH);
        return filename.endsWith(".groovy");
    }
}
