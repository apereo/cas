package org.apereo.cas.mgmt.authz;

import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.io.FileWatcherService;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.authorization.generator.AuthorizationGenerator;
import org.pac4j.core.authorization.generator.SpringSecurityPropertiesAuthorizationGenerator;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * This is {@link CasSpringSecurityAuthorizationGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasSpringSecurityAuthorizationGenerator implements AuthorizationGenerator<CommonProfile> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasSpringSecurityAuthorizationGenerator.class);

    private SpringSecurityPropertiesAuthorizationGenerator generator;

    public CasSpringSecurityAuthorizationGenerator(final Resource usersFile) {
        final Properties properties = new Properties();
        try {
            if (ResourceUtils.doesResourceExist(usersFile)) {
                properties.load(usersFile.getInputStream());
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        this.generator = new SpringSecurityPropertiesAuthorizationGenerator(properties);
        watchResource(usersFile);
    }

    private void watchResource(final Resource usersFile) {
        try {
            final FileWatcherService watcher = new FileWatcherService(usersFile.getFile(),
                    Unchecked.consumer(file -> {
                        final Properties newProps = new Properties();
                        newProps.load(new FileInputStream(file));
                        this.generator = new SpringSecurityPropertiesAuthorizationGenerator(newProps);
                    }));
            watcher.start(getClass().getSimpleName());
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
    }

    @Override
    public CommonProfile generate(final WebContext context, final CommonProfile profile) {
        return this.generator.generate(context, profile);
    }
}
