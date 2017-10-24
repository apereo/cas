package org.apereo.cas.support.saml.mdui.web.flow;

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
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.config.SamlConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.mdui.SamlMetadataUIInfo;
import org.apereo.cas.support.saml.mdui.config.SamlMetadataUIConfiguration;
import org.apereo.cas.support.saml.mdui.config.SamlMetadataUIWebflowConfiguration;
import org.apereo.cas.util.SchedulingUtils;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.config.CasProtocolViewsConfiguration;
import org.apereo.cas.web.config.CasValidationConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
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
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import javax.annotation.PostConstruct;

import static org.junit.Assert.*;

/**
 * This is {@link SamlMetadataUIParserDynamicActionTests}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                SamlMetadataUIParserDynamicActionTests.CasTestConfiguration.class,
                SamlMetadataUIConfiguration.class,
                SamlMetadataUIWebflowConfiguration.class,
                CasDefaultServiceTicketIdGeneratorsConfiguration.class,
                CasCoreTicketIdGeneratorsConfiguration.class,
                CasWebApplicationServiceFactoryConfiguration.class,
                CasCoreAuthenticationConfiguration.class, 
                CasCoreServicesAuthenticationConfiguration.class,
                CasCoreServicesConfiguration.class,
                CoreSamlConfiguration.class,
                CasCoreWebConfiguration.class,
                CasCoreWebflowConfiguration.class,
                RefreshAutoConfiguration.class,
                AopAutoConfiguration.class,
                CasCookieConfiguration.class,
                CasCoreAuthenticationPrincipalConfiguration.class,
                CasCoreAuthenticationPolicyConfiguration.class,
                CasCoreAuthenticationMetadataConfiguration.class,
                CasCoreAuthenticationSupportConfiguration.class,
                CasCoreAuthenticationHandlersConfiguration.class,
                CasCoreHttpConfiguration.class,
                CasCoreTicketsConfiguration.class,
                CasCoreTicketCatalogConfiguration.class,
                CasCoreLogoutConfiguration.class,
                CasValidationConfiguration.class,
                CasProtocolViewsConfiguration.class,
                CasCoreValidationConfiguration.class,
                CasCoreConfiguration.class,
                CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
                SamlConfiguration.class,
                CasPersonDirectoryConfiguration.class,
                CasCoreUtilConfiguration.class})
@TestPropertySource(properties = "cas.samlMetadataUi.resources=http://mdq-beta.incommon.org/global/entities/::")
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
public class SamlMetadataUIParserDynamicActionTests extends AbstractOpenSamlTests {

    @Autowired
    @Qualifier("samlMetadataUIParserAction")
    private Action samlMetadataUIParserAction;

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
    public void verifyEntityIdUIInfoExistsDynamically() throws Exception {
        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID, "https://carmenwiki.osu.edu/shibboleth");

        final MockHttpServletResponse response = new MockHttpServletResponse();

        final MockServletContext sCtx = new MockServletContext();
        ctx.setExternalContext(new ServletExternalContext(sCtx, request, response));
        samlMetadataUIParserAction.execute(ctx);
        assertNotNull(WebUtils.getServiceUserInterfaceMetadata(ctx, SamlMetadataUIInfo.class));
    }


}
