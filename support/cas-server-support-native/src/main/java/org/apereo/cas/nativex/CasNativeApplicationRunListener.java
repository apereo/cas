package org.apereo.cas.nativex;

import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.configuration.DefaultCasConfigurationPropertiesSourceLocator;
import org.apereo.cas.configuration.StandaloneConfigurationFilePropertiesSourceLocator;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.DefaultResourceLoader;

/**
 * This is {@link CasNativeApplicationRunListener}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
class CasNativeApplicationRunListener implements SpringApplicationRunListener {
    CasNativeApplicationRunListener(final SpringApplication application, final String[] args) {
    }

    @Override
    public void environmentPrepared(final @NonNull ConfigurableBootstrapContext bootstrapContext,
                                    final @NonNull ConfigurableEnvironment environment) {
        val nativeSources = CasConfigurationPropertiesEnvironmentManager.configureEnvironmentPropertySources(environment);
        val configurationCipher = new CasConfigurationJasyptCipherExecutor(environment);
        val casConfigLocator = new DefaultCasConfigurationPropertiesSourceLocator(configurationCipher);
        val standaloneLocator = new StandaloneConfigurationFilePropertiesSourceLocator(configurationCipher);
        val resourceLoader = new DefaultResourceLoader();
        casConfigLocator.locate(environment, resourceLoader).ifPresent(nativeSources::addPropertySource);
        standaloneLocator.locate(environment, resourceLoader).ifPresent(nativeSources::addPropertySource);
        environment.getPropertySources().addFirst(nativeSources);
    }
}
