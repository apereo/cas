package org.apereo.cas.support.saml.mdui.web.flow;

import module java.base;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasSamlMetadataUIAutoConfiguration;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.mdui.SamlMetadataUIInfo;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlMetadataUIParserDynamicActionTests}.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@SpringBootTest(classes = {
    CasCoreWebflowAutoConfiguration.class,
    CasSamlMetadataUIAutoConfiguration.class,
    AbstractOpenSamlTests.SharedTestConfiguration.class
}, properties = "cas.saml-metadata-ui.resources=")
@Tag("SAMLMetadata")
@ExtendWith(CasTestExtension.class)
class SamlMetadataUIParserDynamicActionTests extends AbstractOpenSamlTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_SAML_METADATA_UI_PARSER)
    private Action samlMetadataUIParserAction;

    @Test
    void verifyEntityIdUIInfoExistsDynamically() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(SamlProtocolConstants.PARAMETER_ENTITY_ID, "https://carmenwiki.osu.edu/shibboleth");
        samlMetadataUIParserAction.execute(context);
        assertNotNull(WebUtils.getServiceUserInterfaceMetadata(context, SamlMetadataUIInfo.class));
    }


}
