package org.apereo.cas.support.oauth.web;

import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasOAuthConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAttributeRepositoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * This is {@link AbstractOAuth20Tests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                CasCoreAuthenticationConfiguration.class,
                CasCoreServicesConfiguration.class,
                CasOAuthConfiguration.class,
                CasCoreTicketsConfiguration.class,
                CasPersonDirectoryAttributeRepositoryConfiguration.class,
                RefreshAutoConfiguration.class,
                CasCoreLogoutConfiguration.class,
                CasCoreUtilConfiguration.class})
@DirtiesContext
@ContextConfiguration(locations = "classpath:/oauth-context.xml")
@ComponentScan(basePackages = {"org.pac4j.springframework", "org.apereo.cas"})
@EnableConfigurationProperties(CasConfigurationProperties.class)
public abstract class AbstractOAuth20Tests {

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @PostConstruct
    public void init() {
        authenticationHandlersResolvers.put(new SimpleTestUsernamePasswordAuthenticationHandler(),
                personDirectoryPrincipalResolver);
    }
}
