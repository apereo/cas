package org.apereo.cas.support.saml.mdui.web.flow;

import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.mdui.SamlMetadataUIInfo;
import org.apereo.cas.support.saml.mdui.config.SamlMetadataUIConfiguration;
import org.apereo.cas.support.saml.mdui.config.SamlMetadataUIWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlMetadataUIParserDynamicActionTests}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@SpringBootTest(classes = {
    CasCoreWebflowConfiguration.class,
    CasWebflowContextConfiguration.class,
    SamlMetadataUIConfiguration.class,
    SamlMetadataUIWebflowConfiguration.class,
    AbstractOpenSamlTests.SharedTestConfiguration.class
}, properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "cas.saml-metadata-ui.resources=http://mdq-beta.incommon.org/global/entities/::"
})
@Tag("SAML")
public class SamlMetadataUIParserDynamicActionTests extends AbstractOpenSamlTests {
    @Autowired
    @Qualifier("samlMetadataUIParserAction")
    private Action samlMetadataUIParserAction;

    @Test
    public void verifyEntityIdUIInfoExistsDynamically() throws Exception {
        val ctx = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID, "https://carmenwiki.osu.edu/shibboleth");

        val response = new MockHttpServletResponse();

        val sCtx = new MockServletContext();
        ctx.setExternalContext(new ServletExternalContext(sCtx, request, response));
        samlMetadataUIParserAction.execute(ctx);
        assertNotNull(WebUtils.getServiceUserInterfaceMetadata(ctx, SamlMetadataUIInfo.class));
    }


}
