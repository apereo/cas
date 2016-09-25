package org.apereo.cas.support.saml.web.flow.mdui;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAttributeRepositoryConfiguration;
import org.apereo.cas.config.CoreSamlConfiguration;
import org.apereo.cas.config.SamlConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.web.flow.SamlMetadataUIParserAction;
import org.apereo.cas.support.saml.web.flow.config.SamlMetadataUIConfiguration;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.config.CasProtocolViewsConfiguration;
import org.apereo.cas.web.config.CasValidationConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link SamlMetadataUIParserActionTests}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {SamlMetadataUIConfiguration.class,
                CasCoreAuthenticationConfiguration.class,
                CasCoreServicesConfiguration.class,
                CoreSamlConfiguration.class,
                CasCoreWebConfiguration.class,
                CasCoreWebflowConfiguration.class,
                RefreshAutoConfiguration.class,
                AopAutoConfiguration.class,
                CasCookieConfiguration.class,
                CasCoreAuthenticationConfiguration.class,
                CasCoreTicketsConfiguration.class,
                CasCoreLogoutConfiguration.class,
                CasValidationConfiguration.class,
                CasProtocolViewsConfiguration.class,
                CasCoreValidationConfiguration.class,
                CasCoreConfiguration.class,
                SamlConfiguration.class,
                CasPersonDirectoryAttributeRepositoryConfiguration.class,
                CasCoreUtilConfiguration.class})
@TestPropertySource(properties = {"cas.samlMetadataUi.resources=classpath:sample-metadata.xml::classpath:inc-md-pub.pem"})
@WebAppConfiguration
public class SamlMetadataUIParserActionTests extends AbstractOpenSamlTests {

    @Autowired
    @Qualifier("samlMetadataUIParserAction")
    private SamlMetadataUIParserAction samlMetadataUIParserAction;

    @Test
    public void verifyEntityIdUIInfoExists() throws Exception {
        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID, "https://carmenwiki.osu.edu/shibboleth");

        final MockHttpServletResponse response = new MockHttpServletResponse();

        final MockServletContext sCtx = new MockServletContext();
        ctx.setExternalContext(new ServletExternalContext(sCtx, request, response));
        samlMetadataUIParserAction.doExecute(ctx);
        assertTrue(ctx.getFlowScope().contains(SamlMetadataUIParserAction.MDUI_FLOW_PARAMETER_NAME));
    }


    @Test
    public void verifyEntityIdUIInfoNoParam() throws Exception {
        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("somethingelse", "https://carmenwiki.osu.edu/shibboleth");

        final MockHttpServletResponse response = new MockHttpServletResponse();

        final MockServletContext sCtx = new MockServletContext();
        ctx.setExternalContext(new ServletExternalContext(sCtx, request, response));
        samlMetadataUIParserAction.doExecute(ctx);
        assertFalse(ctx.getFlowScope().contains(SamlMetadataUIParserAction.MDUI_FLOW_PARAMETER_NAME));
    }

}
