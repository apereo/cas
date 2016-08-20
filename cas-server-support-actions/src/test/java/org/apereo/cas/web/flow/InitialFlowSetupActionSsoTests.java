package org.apereo.cas.web.flow;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAttributeRepositoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.config.CasSupportActionsConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.test.MockRequestContext;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringApplicationConfiguration(
        classes = {CasSupportActionsConfiguration.class, 
                CasCoreWebflowConfiguration.class,
                CasCoreWebConfiguration.class,
                CasCoreConfiguration.class,
                CasCoreTicketsConfiguration.class,
                CasCoreLogoutConfiguration.class,
                CasCoreAuthenticationConfiguration.class,
                CasPersonDirectoryAttributeRepositoryConfiguration.class,
                CasCookieConfiguration.class,
                RefreshAutoConfiguration.class,
                CasCoreServicesConfiguration.class},
        locations = {
                "classpath:/core-context.xml"
        },
        initializers = ConfigFileApplicationContextInitializer.class)
@TestPropertySource(properties = "cas.sso.missingService=false")
public class InitialFlowSetupActionSsoTests {

    @Autowired
    @Qualifier("initialFlowSetupAction")
    private Action action;

    @Test(expected = NoSuchFlowExecutionException.class)
    public void disableFlowIfNoService() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        this.action.execute(context);
    }
}
