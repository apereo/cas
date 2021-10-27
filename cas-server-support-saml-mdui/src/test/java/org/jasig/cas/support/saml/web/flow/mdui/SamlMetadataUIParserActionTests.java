package org.jasig.cas.support.saml.web.flow.mdui;

import org.jasig.cas.support.saml.AbstractOpenSamlTests;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link SamlMetadataUIParserActionTests}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class SamlMetadataUIParserActionTests extends AbstractOpenSamlTests {

    @Autowired
    @Qualifier("samlMetadataUIParserAction")
    private SamlMetadataUIParserAction samlMetadataUIParserAction;

    @Autowired
    @Qualifier("samlDynamicMetadataUIParserAction")
    private SamlMetadataUIParserAction samlDynamicMetadataUIParserAction;

    @Test
    public void verifyEntityIdUIInfoExists() throws Exception {
        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SamlMetadataUIParserAction.ENTITY_ID_PARAMETER_NAME, "https://carmenwiki.osu.edu/shibboleth");

        final MockHttpServletResponse response = new MockHttpServletResponse();

        final MockServletContext sCtx = new MockServletContext();
        ctx.setExternalContext(new ServletExternalContext(sCtx, request, response));
        samlMetadataUIParserAction.doExecute(ctx);
        assertTrue(ctx.getFlowScope().contains(SamlMetadataUIParserAction.MDUI_FLOW_PARAMETER_NAME));
    }

    @Test
    public void verifyEntityIdUIInfoExistsDynamically() throws Exception {
        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(SamlMetadataUIParserAction.ENTITY_ID_PARAMETER_NAME, "https://carmenwiki.osu.edu/shibboleth");

        final MockHttpServletResponse response = new MockHttpServletResponse();

        final MockServletContext sCtx = new MockServletContext();
        ctx.setExternalContext(new ServletExternalContext(sCtx, request, response));
        samlDynamicMetadataUIParserAction.doExecute(ctx);
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
