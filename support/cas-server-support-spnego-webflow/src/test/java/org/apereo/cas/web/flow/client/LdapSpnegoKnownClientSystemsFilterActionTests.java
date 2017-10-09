package org.apereo.cas.web.flow.client;

import org.apereo.cas.adaptors.ldap.AbstractLdapTests;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.SpnegoConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.util.SchedulingUtils;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.SpnegoWebflowActionsConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import javax.annotation.PostConstruct;

import static org.junit.Assert.*;

/**
 * Test cases for {@link LdapSpnegoKnownClientSystemsFilterAction}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        LdapSpnegoKnownClientSystemsFilterActionTests.CasTestConfiguration.class,
        SpnegoConfiguration.class,
        SpnegoWebflowActionsConfiguration.class,
        RefreshAutoConfiguration.class,
        CasCoreServicesConfiguration.class,
        CasPersonDirectoryConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
        CasCoreTicketsConfiguration.class,
        CasCoreTicketCatalogConfiguration.class,
        CasCoreLogoutConfiguration.class,
        CasCookieConfiguration.class,
        CasCoreWebflowConfiguration.class,
        CasWebApplicationServiceFactoryConfiguration.class,
        CasCoreAuthenticationConfiguration.class, 
        CasCoreServicesAuthenticationConfiguration.class,
        CasCoreAuthenticationPrincipalConfiguration.class,
        CasCoreAuthenticationPolicyConfiguration.class,
        CasCoreAuthenticationMetadataConfiguration.class,
        CasCoreAuthenticationSupportConfiguration.class,
        CasCoreAuthenticationHandlersConfiguration.class,
        CasCoreHttpConfiguration.class})
@TestPropertySource(locations = {"classpath:/spnego.properties"})
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
public class LdapSpnegoKnownClientSystemsFilterActionTests extends AbstractLdapTests {

    @Autowired
    @Qualifier("ldapSpnegoClientAction")
    private Action action;

    @BeforeClass
    public static void bootstrap() throws Exception {
        initDirectoryServer(1381);
    }

    @TestConfiguration
    public static class CasTestConfiguration {
        @Autowired
        protected ApplicationContext applicationContext;

        @PostConstruct
        public void init() {
            SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
        }
    }

    
    @Test
    public void ensureLdapAttributeShouldDoSpnego() throws Exception {
        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("localhost");
        final ServletExternalContext extCtx = new ServletExternalContext(
                new MockServletContext(), req,
                new MockHttpServletResponse());
        ctx.setExternalContext(extCtx);

        final Event ev = action.execute(ctx);
        assertEquals(ev.getId(), new EventFactorySupport().yes(this).getId());
    }
}
