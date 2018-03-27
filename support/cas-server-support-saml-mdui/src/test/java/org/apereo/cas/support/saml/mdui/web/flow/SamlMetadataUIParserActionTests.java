package org.apereo.cas.support.saml.mdui.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.mdui.SamlMetadataUIInfo;
import org.apereo.cas.support.saml.mdui.config.SamlMetadataUIConfiguration;
import org.apereo.cas.support.saml.mdui.config.SamlMetadataUIWebflowConfiguration;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link SamlMetadataUIParserActionTests}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@RunWith(SpringRunner.class)
@Import({SamlMetadataUIConfiguration.class, SamlMetadataUIWebflowConfiguration.class})
@TestPropertySource(properties = {"cas.samlMetadataUi.resources=classpath:sample-metadata.xml::classpath:inc-md-pub.pem"})
@Slf4j
public class SamlMetadataUIParserActionTests extends AbstractOpenSamlTests {

    @Autowired
    @Qualifier("samlMetadataUIParserAction")
    private Action samlMetadataUIParserAction;

    @Test
    public void verifyEntityIdUIInfoExists() throws Exception {
        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID, "https://carmenwiki.osu.edu/shibboleth");
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockServletContext sCtx = new MockServletContext();
        ctx.setExternalContext(new ServletExternalContext(sCtx, request, response));
        ctx.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, RegisteredServiceTestUtils.getService());
        samlMetadataUIParserAction.execute(ctx);
        assertNotNull(WebUtils.getServiceUserInterfaceMetadata(ctx, SamlMetadataUIInfo.class));
    }


    @Test
    public void verifyEntityIdUIInfoNoParam() throws Exception {
        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("somethingelse", "https://carmenwiki.osu.edu/shibboleth");

        final MockHttpServletResponse response = new MockHttpServletResponse();

        final MockServletContext sCtx = new MockServletContext();
        ctx.setExternalContext(new ServletExternalContext(sCtx, request, response));
        samlMetadataUIParserAction.execute(ctx);
        assertNull(WebUtils.getServiceUserInterfaceMetadata(ctx, SamlMetadataUIInfo.class));
    }

}
